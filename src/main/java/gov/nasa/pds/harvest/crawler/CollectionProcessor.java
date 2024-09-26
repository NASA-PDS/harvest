package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import gov.nasa.pds.registry.common.util.CloseUtils;
import org.w3c.dom.Document;
import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.util.xml.XmlIs;
import gov.nasa.pds.registry.common.cfg.BundleType;
import gov.nasa.pds.registry.common.cfg.ConfigManager;
import gov.nasa.pds.registry.common.cfg.HarvestConfigurationType;
import gov.nasa.pds.registry.common.es.service.CollectionInventoryWriter;
import gov.nasa.pds.registry.common.meta.CollectionMetadataExtractor;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;


/**
 * Process "Product_Collection" products (PDS4 XML label files).
 * 
 * <p> Processing steps:
 * <ul>
 * <li>Crawl file system</li>
 * <li>Parse PDS4 labels (XML files)</li>
 * <li>Extract product metadata</li>
 * <li>Write extracted metadata into a JSON or XML file</li>
 * <li>Generated JSON files can be imported into Elasticsearch by Registry manager tool.</li>
 * </ul>
 *  
 * @author karpenko
 */
public class CollectionProcessor extends BaseProcessor
{
    private CollectionInventoryProcessor invProc;
    private CollectionInventoryWriter invWriter;
    
    private CollectionMetadataExtractor collectionExtractor;
    
    private int collectionCount;

    
    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @throws Exception Generic exception
     */
    public CollectionProcessor(HarvestConfigurationType config, String archive_status) throws Exception
    {
        super(config, archive_status);
        invWriter = new CollectionInventoryWriter(ConfigManager.exchangeRegistry(config.getRegistry()));
        this.invProc = new CollectionInventoryProcessor(config.getReferences().isPrimaryOnly());
        collectionExtractor = new CollectionMetadataExtractor();
    }
    

    /**
     * Inner class used by Files.find() to select collection label files.
     * @author karpenko
     */
    private static class CollectionMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            return XmlIs.aCollection(path.toString());
        }
    }


    /**
     * Process collections of a bundle.
     * Bundle configuration is provided in a Harvest configuration file.
     * @param bCfg Bundle configuration parameters
     * @return number of processed collections (0 or more)
     * @throws Exception Generic exception
     */
    public int process(BundleType bCfg) throws Exception {
        collectionCount = 0;

        File bundleDir = new File(bCfg.getDir());
        Stream<Path> stream = null;

        try {
            stream = Files.find(bundleDir.toPath(), 2, new CollectionMatcher(), FileVisitOption.FOLLOW_LINKS);
            Iterator<Path> it = stream.iterator();

            while (it.hasNext()) {
                onCollection(it.next().toFile(), bCfg);
            }
        } finally {
            CloseUtils.close(stream);
        }

        return collectionCount;
    }


    private void onCollection(File file, BundleType bCfg) throws Exception
    {
        // Skip very large files
        if(file.length() > MAX_XML_FILE_LENGTH)
        {
            log.warn("File is too big to parse: " + file.getAbsolutePath());
            return;
        }

        Document doc = XmlDomUtils.readXml(dbf, file);
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Ignore non-collection XMLs
        if(!"Product_Collection".equals(rootElement)) return;
        
        processMetadata(file, doc, bCfg);
    }
    
    
    private void processMetadata(File file, Document doc, BundleType bCfg) throws Exception
    {
        Metadata meta = basicExtractor.extract(file, doc, this.archive_status);
        meta.setNodeName(ConfigManager.exchangeIndexForNode(RegistryManager.getInstance().getIndexName()));

        // Collection filter
        List<String> lids = ConfigManager.exchangeLids (bCfg.getCollection());
        List<String> lidvids = ConfigManager.exchangeLidvids (bCfg.getCollection());
        if(!lids.isEmpty() && !lids.contains(meta.lid)) return;
        if(!lidvids.isEmpty() && !lidvids.contains(meta.lidvid)) return;

        // Ignore collections not listed in bundles
        LidVidCache cache = RefsCache.getInstance().getCollectionRefsCache();
        if(!cache.containsLidVid(meta.lidvid) && !cache.containsLid(meta.lid)) return;
        
        log.info("Processing collection " + file.getAbsolutePath());
        collectionCount++;
        
        RegistryDao dao = RegistryManager.getInstance().getRegistryDao();
        Counter counter = RegistryManager.getInstance().getCounter();

        boolean overwriteMode = RegistryManager.getInstance().isOverwrite();
        boolean collectionAlreadyRegistered = dao.idExists(meta.lidvid);

        if(collectionAlreadyRegistered && !overwriteMode)
        {
            log.warn("Collection " + meta.lidvid + " already registered. Skipping.");
            
            // Only cache but don't write product references
            processInventoryFiles(file, doc, meta, false);
            
            counter.skippedFileCount++;
            return;
        }
        
        // Internal references
        refExtractor.addRefs(meta.intRefs, doc);
        
        // Extract fields by XPath (if configured)
        xpathExtractor.extract(doc, meta.fields);
        
        // Extract all fields as key-value pairs
        XmlNamespaces nsInfo = autogenExtractor.extract(file, meta.fields);
        
        // Search fields
        searchExtractor.extract(doc, meta.fields);

        // File information (name, size, checksum)
        fileDataExtractor.extract(file, meta, ConfigManager.exchangeFileRef(config.getFileInfo().getFileRef()));
        
        // Save metadata
        save(meta, nsInfo);
        
        // Cache and write product references
        processInventoryFiles(file, doc, meta, true);
    }

    
    /**
     * Process PDS collection inventory files
     * @param collectionFile PDS collection file
     * @param doc Parsed PDS collection DOM model
     * @param meta PDS collection metadata
     * @param write If true, write and cache products. If false, only cache products.
     * @throws Exception an exception
     */
    private void processInventoryFiles(File collectionFile, Document doc, Metadata meta, boolean write) throws Exception
    {
        Set<String> fileNames = collectionExtractor.extractInventoryFileNames(doc);
        if(fileNames == null) return;
        
        for(String fileName: fileNames)
        {
            File invFile = new File(collectionFile.getParentFile(), fileName);
            
            // Collection is not registered. Write inventory refs.
            if(write)
            {
                // Write inventory refs.
                invWriter.writeCollectionInventory(meta.lidvid, invFile, jobId);
                // Cache non-registered products.
                invProc.cacheNonRegisteredInventory(invFile);
            }
            // Collection is already registered. Only cache non-registered products.
            else
            {
                invProc.cacheNonRegisteredInventory(invFile);
            }
        }
    }

}
