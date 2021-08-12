package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.dao.RegistryDAO;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.CollectionMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.SearchMetadataExtractor;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.out.WriterManager;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


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
public class CollectionProcessor
{
    private Logger log;

    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private CollectionInventoryProcessor invProc;
    
    private DocumentBuilderFactory dbf;
    private BasicMetadataExtractor basicExtractor;
    private CollectionMetadataExtractor collectionExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private SearchMetadataExtractor searchExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private int collectionCount;
    private Counter counter;

    
    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @param counter Counter of processed products
     * @throws Exception Generic exception
     */
    public CollectionProcessor(Configuration config, Counter counter) throws Exception
    {
        this.config = config;
        this.invProc = new CollectionInventoryProcessor(config.refsCfg.primaryOnly);
        this.counter = counter;
        
        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor(config);
        collectionExtractor = new CollectionMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();
        xpathExtractor = new XPathExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
        searchExtractor = new SearchMetadataExtractor();
        fileDataExtractor = new FileMetadataExtractor(config);
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
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml") && fileName.contains("collection"));
        }
    }


    /**
     * Process collections of a bundle.
     * Bundle configuration is provided in a Harvest configuration file.
     * @param bCfg Bundle configuration parameters
     * @return number of processed collections (0 or more)
     * @throws Exception Generic exception
     */
    public int process(BundleCfg bCfg) throws Exception
    {
        collectionCount = 0;
        
        File bundleDir = new File(bCfg.dir);
        Iterator<Path> it = Files.find(bundleDir.toPath(), 2, new CollectionMatcher()).iterator();
        
        while(it.hasNext())
        {
            onCollection(it.next().toFile(), bCfg);
        }
        
        return collectionCount;
    }


    private void onCollection(File file, BundleCfg bCfg) throws Exception
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
    
    
    private void processMetadata(File file, Document doc, BundleCfg bCfg) throws Exception
    {
        Metadata meta = basicExtractor.extract(file, doc);

        // Collection filter
        if(bCfg.collectionLids != null && !bCfg.collectionLids.contains(meta.lid)) return;
        if(bCfg.collectionLidVids != null && !bCfg.collectionLidVids.contains(meta.lidvid)) return;

        // Ignore collections not listed in bundles
        LidVidCache cache = RefsCache.getInstance().getCollectionRefsCache();
        if(!cache.containsLidVid(meta.lidvid) && !cache.containsLid(meta.lid)) return;
        
        log.info("Processing collection " + file.getAbsolutePath());
        collectionCount++;
        
        RegistryDAO dao = (RegistryManager.getInstance() == null) ? null 
                : RegistryManager.getInstance().getRegistryDAO(); 

        // Collection already registered in the Registry (Elasticsearch)
        if(dao != null && dao.idExists(meta.lidvid))
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
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }
        
        // Search fields
        searchExtractor.extract(doc, meta.fields);

        // File information (name, size, checksum)
        fileDataExtractor.extract(file, meta);
        
        RegistryDocWriter writer = WriterManager.getInstance().getRegistryWriter();
        writer.write(meta);
        
        // Cache and write product references
        processInventoryFiles(file, doc, meta, true);
        
        counter.prodCounters.inc(meta.prodClass);
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
                // Registry is configured
                if(RegistryManager.getInstance() != null)
                {
                    // Write inventory refs. Don't cache any products.
                    invProc.writeCollectionInventory(meta, invFile, false);
                    // Cache non-registered products.
                    invProc.cacheNonRegisteredInventory(meta, invFile);
                }
                // Registry is not configured
                else
                {
                    // Write inventory refs and cache all products.
                    invProc.writeCollectionInventory(meta, invFile, true);
                }
            }
            // Collection is already registered. Only cache non-registered products.
            else
            {
                invProc.cacheNonRegisteredInventory(meta, invFile);
            }
        }
    }

}
