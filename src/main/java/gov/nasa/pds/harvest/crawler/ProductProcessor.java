package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import gov.nasa.pds.registry.common.util.CloseUtils;
import org.w3c.dom.Document;
import gov.nasa.pds.harvest.cfg.BundleType;
import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.cfg.ProductType;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.util.out.SupplementalWriter;
import gov.nasa.pds.harvest.util.out.WriterManager;
import gov.nasa.pds.harvest.util.xml.XmlIs;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;


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
public class ProductProcessor extends BaseProcessor
{
    /**
     * Constructor
     * @param config Harvest configuration parameters
     * @throws Exception Generic exception
     */
    public ProductProcessor(HarvestConfigurationType config) throws Exception
    {
        super(config);
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
        public FileMatcher(BundleType bCfg)
        {
            this.includeDirs = new HashSet<String>();
            for (ProductType product : bCfg.getProduct()) {
              this.includeDirs.add (product.getDir());
            }
            File rootDir = new File(bCfg.getDir());
            String rootPath = rootDir.toPath().toUri().toString();
            startIndex = rootPath.length();
        }

        
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            if(!XmlIs.aLabel(path.toString())) return false;

            if(includeDirs.size() == 0) return true;
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
     * @throws Exception Generic exception
     */
    public void process(BundleType bCfg) throws Exception {
        log.info("Processing products...");

        FileMatcher matcher = new FileMatcher(bCfg);

        File bundleDir = new File(bCfg.getDir());
        Stream<Path> stream = null;

        try {
            stream = Files.find(bundleDir.toPath(), 20, matcher, FileVisitOption.FOLLOW_LINKS);
            Iterator<Path> it = stream.iterator();

            while (it.hasNext()) {
                onFile(it.next().toFile());
            }
        } finally {
            CloseUtils.close(stream);
        }
    }


    /**
     * Process one file
     * @param file PDS label file
     * @throws Exception Generic exception
     */
    public void onFile(File file) throws Exception
    {
        Document doc = null;
        Counter counter = RegistryManager.getInstance().getCounter();
        
        try
        {
            // Skip very large files
            if(file.length() > MAX_XML_FILE_LENGTH)
            {
                log.warn("File is too big to parse: " + file.getAbsolutePath());
                counter.skippedFileCount++;
                return;
            }

            doc = XmlDomUtils.readXml(dbf, file);
        }
        catch(Exception ex)
        {
            log.warn(ex.getMessage());
            counter.failedFileCount++;
            return;
        }        
        
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Ignore collections and bundles
        if("Product_Bundle".equals(rootElement) || "Product_Collection".equals(rootElement)) return;

        // Apply product filter
        if (!config.getProductFilter().getInclude().isEmpty() &&
            !config.getProductFilter().getInclude().contains(rootElement)) return;
        if(config.getProductFilter().getExclude().contains(rootElement)) return;

        // Process metadata
        try
        {
            processMetadata(file, doc);
        }
        catch(Exception ex)
        {
            log.error(ex.getMessage());
            counter.failedFileCount++;
        }        
    }
    
    
    /**
     * Extract metadata from a label file.
     * @param file PDS label file
     * @param doc XML DOM model of the PDS label file
     * @throws Exception Generic exception
     */
    private void processMetadata(File file, Document doc) throws Exception
    {
        Counter counter = RegistryManager.getInstance().getCounter();
        
        // Extract basic metadata
        Metadata meta = basicExtractor.extract(file, doc);
        meta.setNodeName("fixme or delete me");

        // Only process primary products from collection inventory
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        // productNotInCache may be equivalent to productAlreadyRegistered, but it's unclear if there are other factors
        // which might result in this being true, so I'm keeping it explicit until I know otherwise
        boolean productNotInCache = !cache.containsLidVid(meta.lidvid) && !cache.containsLid(meta.lid);
        boolean overwriteMode = RegistryManager.getInstance().isOverwrite();

        if(productNotInCache && !overwriteMode)
        {
            log.info("Skipping product " + file.getAbsolutePath() + " (LIDVID/LID is not in collection inventory or already exists in registry database)");
            counter.skippedFileCount++;
            return;
        }
        
        log.info("Processing product " + file.getAbsolutePath());

        // Internal references
        refExtractor.addRefs(meta.intRefs, doc);
        
        // Extract fields by XPath
        xpathExtractor.extract(doc, meta.fields);

        // Extract fields autogenerated from data dictionary
        XmlNamespaces nsInfo = autogenExtractor.extract(file, meta.fields);

        // Search fields
        searchExtractor.extract(doc, meta.fields);
        
        // Extract file data
        fileDataExtractor.extract(file, meta, ConfigManager.exchangeFileRef(config.getFileInfo().getFileRef()));
        
        // Save metadata
        save(meta, nsInfo);
        
        // Process supplemental products
        String rootElement = doc.getDocumentElement().getNodeName();
        if(rootElement.equals("Product_Metadata_Supplemental"))
        {
            SupplementalWriter swriter = WriterManager.getInstance().getSupplementalWriter();
            swriter.write(file);
        }
    }

}
