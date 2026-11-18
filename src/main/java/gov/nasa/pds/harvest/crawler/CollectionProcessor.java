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
import gov.nasa.pds.harvest.cfg.BundleType;
import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.util.xml.XmlIs;
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
    
    // Collections are queued here and written after all products
    private final DeferredAggregates aggregates;

    
    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @param archive_status archive status of the products
     * @param aggregates queue for the collections, which are written after all products
     * @throws Exception Generic exception
     */
    public CollectionProcessor(HarvestConfigurationType config, String archive_status, DeferredAggregates aggregates) throws Exception
    {
        super(config, archive_status);
        this.aggregates = aggregates;
        invWriter = RegistryManager.getInstance().getCollectionInventoryWriter();
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
        // Fail now, not after the products are written, if the index is not supported 
        meta.setNodeName(ConfigManager.exchangeIndexForNode(RegistryManager.getInstance().getIndexName()));

        // Collection filter
        List<String> lids = ConfigManager.exchangeLids (bCfg.getCollection());
        List<String> lidvids = ConfigManager.exchangeLidvids (bCfg.getCollection());
        if(!lids.isEmpty() && !lids.contains(meta.lid)) return;
        if(!lidvids.isEmpty() && !lidvids.contains(meta.lidvid)) return;

        // Ignore collections not listed in bundles
        LidVidCache cache = RefsCache.getInstance().getCollectionRefsCache();
        if(!cache.containsLidVid(meta.lidvid) && !cache.containsLid(meta.lid)) return;
        
        collectionCount++;
        
        // The product processor needs the products of the collection inventory now
        // (it only processes products that are not registered yet). 
        cacheInventoryProducts(file, doc);
        
        // Write the collection and its product references after all products
        log.info("Deferring collection until all products are written: " + file.getAbsolutePath());
        aggregates.deferCollection(() -> writeCollection(file));
    }


    /**
     * Write one collection and its product references. Runs after all products of the
     * Harvest run are written.
     * @param file PDS collection label file
     * @throws Exception Generic exception
     */
    private void writeCollection(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(dbf, file);

        Metadata meta = basicExtractor.extract(file, doc, this.archive_status);
        meta.setNodeName(ConfigManager.exchangeIndexForNode(RegistryManager.getInstance().getIndexName()));

        log.info("Processing collection " + file.getAbsolutePath());
        
        RegistryDao dao = RegistryManager.getInstance().getRegistryDao();
        Counter counter = RegistryManager.getInstance().getCounter();

        boolean overwriteMode = RegistryManager.getInstance().isOverwrite();
        boolean collectionAlreadyRegistered = dao.idExists(meta.lidvid);

        if(collectionAlreadyRegistered && !overwriteMode)
        {
            log.warn("Collection " + meta.lidvid + " already registered. Skipping.");
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
        
        // Write product references
        writeInventoryRefs(file, doc, meta);
    }

    
    /**
     * Cache the products of the collection inventory that are not registered yet.
     * @param collectionFile PDS collection file
     * @param doc Parsed PDS collection DOM model
     * @throws Exception an exception
     */
    private void cacheInventoryProducts(File collectionFile, Document doc) throws Exception
    {
        Set<String> fileNames = collectionExtractor.extractInventoryFileNames(doc);
        if(fileNames == null) return;
        
        for(String fileName: fileNames)
        {
            invProc.cacheNonRegisteredInventory(new File(collectionFile.getParentFile(), fileName));
        }
    }

    
    /**
     * Write PDS collection inventory files as product references
     * @param collectionFile PDS collection file
     * @param doc Parsed PDS collection DOM model
     * @param meta PDS collection metadata
     * @throws Exception an exception
     */
    private void writeInventoryRefs(File collectionFile, Document doc, Metadata meta) throws Exception
    {
        Set<String> fileNames = collectionExtractor.extractInventoryFileNames(doc);
        if(fileNames == null) return;
        
        for(String fileName: fileNames)
        {
            invWriter.writeCollectionInventory(meta.lidvid, new File(collectionFile.getParentFile(), fileName), jobId);
        }
    }

}
