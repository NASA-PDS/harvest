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
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


/**
 * Process products (PDS4 XML label files) excluding collections and bundles.
 *
 * <p> Processing steps:
 * <ul>
 * <li>Crawl file system</li>
 * <li>Parse PDS4 labels (XML files)</li>
 * <li>Extract product metadata</li>
 * <li>Write extracted metadata into a JSON or XML file.</li> 
 * <li>Generated JSON files can be imported into Elasticsearch by Registry manager tool.</li>
 * </ul>
 *  
 * @author karpenko
 */
public class ProductProcessor
{
    private Logger log;

    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private RegistryDocWriter writer;
    
    private DocumentBuilderFactory dbf;
    private BasicMetadataExtractor basicExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private FileMetadataExtractor fileDataExtractor;
    private XPathExtractor xpathExtractor;
    
    private Counter counter;
    
    
    /**
     * Constructor
     * @param config Harvest configuration parameters
     * @param writer A writer to write JSON or XML data files with metadata
     * extracted from PDS4 labels. Generated JSON files can be imported 
     * into Elasticsearch by Registry manager tool.
     * @param counter document / product counter
     * @throws Exception
     */
    public ProductProcessor(Configuration config, RegistryDocWriter writer, Counter counter) throws Exception
    {
        log = LogManager.getLogger(getClass());
        this.writer = writer;
        this.counter = counter;

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
        fileDataExtractor = new FileMetadataExtractor(config);
        xpathExtractor = new XPathExtractor();
        
        this.config = config;
    }

    
    /**
     * Inner class used by Files.find() to select product label files.
     * @author karpenko
     */
    private static class FileMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        private Set<String> includeDirs;
        private int startIndex;
        
        
        /**
         * Constructor
         * @param bCfg Bundle configuration
         */
        public FileMatcher(BundleCfg bCfg)
        {
            this.includeDirs = bCfg.productDirs;
        
            File rootDir = new File(bCfg.dir);
            String rootPath = rootDir.toPath().toUri().toString();
            startIndex = rootPath.length();
        }

        
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            if(!fileName.endsWith(".xml")) return false;

            if(includeDirs == null) return true;
            String fileDir = path.getParent().toUri().toString().toLowerCase();
            
            for(String dir: includeDirs)
            {
                // Search the relative path only
                if(fileDir.indexOf(dir, startIndex) >= 0) return true;
            }
            
            return false;
        }
    }

    
    /**
     * Process products of a bundle
     * @param bCfg Bundle configuration
     * @throws Exception
     */
    public void process(BundleCfg bCfg) throws Exception
    {
        log.info("Processing products...");

        FileMatcher matcher = new FileMatcher(bCfg);
        
        File bundleDir = new File(bCfg.dir);
        Iterator<Path> it = Files.find(bundleDir.toPath(), 20, matcher).iterator();
        
        while(it.hasNext())
        {
            onFile(it.next().toFile());
        }
    }
    
    
    /**
     * Process one file
     * @param file
     * @throws Exception
     */
    public void onFile(File file) throws Exception
    {
        // Skip very large files
        if(file.length() > MAX_XML_FILE_LENGTH)
        {
            log.warn("File is too big to parse: " + file.getAbsolutePath());
            return;
        }

        Document doc = XmlDomUtils.readXml(dbf, file);
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Ignore collections and bundles
        if("Product_Bundle".equals(rootElement) || "Product_Collection".equals(rootElement)) return;

        // Apply product filter
        if(config.filters.prodClassInclude != null)
        {
            if(!config.filters.prodClassInclude.contains(rootElement)) return;
        }
        else if(config.filters.prodClassExclude != null)
        {
            if(config.filters.prodClassExclude.contains(rootElement)) return;
        }

        processMetadata(file, doc);
    }
    
    
    /**
     * Extract metadata from a label file.
     * @param file
     * @param doc
     * @throws Exception
     */
    private void processMetadata(File file, Document doc) throws Exception
    {
        // Extract basic metadata
        Metadata meta = basicExtractor.extract(file, doc);
        meta.fields.addValue(Constants.FLD_NODE_NAME, config.nodeName);

        // Only process primary products from collection inventory
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        if(!cache.containsLidVid(meta.lidvid) && !cache.containsLid(meta.lid)) 
        {
            counter.skippedFileCount++;
            return;
        }
        
        log.info("Processing product " + file.getAbsolutePath());

        // Internal references
        refExtractor.addRefs(meta.intRefs, doc);
        
        // Extract fields by XPath
        xpathExtractor.extract(doc, meta.fields);

        // Extract fields autogenerated from data dictionary
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }

        // Extract file data
        fileDataExtractor.extract(file, meta);
        
        writer.write(meta);
        
        counter.prodCounters.inc(meta.prodClass);
    }

}
