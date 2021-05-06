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

import gov.nasa.pds.harvest.Constants;
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
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
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
 * </p>
 *  
 * @author karpenko
 */
public class CollectionProcessor
{
    private Logger log;

    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private RegistryDocWriter writer;
    private CollectionInventoryProcessor invProc;
    
    private DocumentBuilderFactory dbf;
    private BasicMetadataExtractor basicExtractor;
    private CollectionMetadataExtractor collectionExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private int collectionCount;
    private Counter counter;

    
    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @param writer Registry document writer (JSON or XML)
     * @param refsWriter
     * @param counter
     * @throws Exception
     */
    public CollectionProcessor(Configuration config, RegistryDocWriter writer, 
            RefsDocWriter refsWriter, Counter counter) throws Exception
    {
        this.config = config;
        this.writer = writer;
        this.invProc = new CollectionInventoryProcessor(refsWriter, config.refsCfg.primaryOnly);
        this.counter = counter;
        
        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor();
        collectionExtractor = new CollectionMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();
        xpathExtractor = new XPathExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
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
     * @return
     * @throws Exception
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
        meta.fields.addValue(Constants.FLD_NODE_NAME, config.nodeName);

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
            log.warn("Collection " + meta.lidvid + " already registered");
            
            // Only cache but don't write product references
            processInventoryFiles(file, doc, meta, false);
            
            counter.skippedFileCount++;
            return;
        }
        
        refExtractor.addRefs(meta.intRefs, doc);
        xpathExtractor.extract(doc, meta.fields);
        
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }
        
        fileDataExtractor.extract(file, meta);        
        writer.write(meta);
        
        // Cache and write product references
        processInventoryFiles(file, doc, meta, true);
        
        counter.prodCounters.inc(meta.prodClass);
    }

    
    private void processInventoryFiles(File collectionFile, Document doc, Metadata meta, boolean write) throws Exception
    {
        Set<String> fileNames = collectionExtractor.extractInventoryFileNames(doc);
        if(fileNames == null) return;
        
        for(String fileName: fileNames)
        {
            File invFile = new File(collectionFile.getParentFile(), fileName);
            if(write)
            {
                invProc.writeCollectionInventory(meta, invFile, true);
            }
            else
            {
                invProc.cacheNonRegisteredInventory(meta, invFile);
            }
        }
    }

}
