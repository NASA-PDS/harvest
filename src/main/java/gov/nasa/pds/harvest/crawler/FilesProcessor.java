package gov.nasa.pds.harvest.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.BundleMetadataExtractor;
import gov.nasa.pds.harvest.meta.CollectionMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.SearchMetadataExtractor;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.CloseUtils;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.out.SupplementalWriter;
import gov.nasa.pds.harvest.util.out.WriterManager;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;
import gov.nasa.pds.harvest.util.xml.XmlNamespaces;


/**
 * Process PDS label files in a directory.
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
public class FilesProcessor
{
    private Logger log;
    
    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private DocumentBuilderFactory dbf;

    // Bundle and Collection extractors & processors
    private BundleMetadataExtractor bundleExtractor;
    private CollectionMetadataExtractor collectionExtractor;
    private CollectionInventoryProcessor invProc;
    
    // Common extractors
    private BasicMetadataExtractor basicExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private SearchMetadataExtractor searchExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private Counter counter;
    
    
    /**
     * Constructor
     * @param config Harvest configuration parameters (from a config file)
     * @param counter Counter of processed products by type
     * @throws Exception Generic exception
     */
    public FilesProcessor(Configuration config, Counter counter) throws Exception
    {
        this.config = config;
        this.invProc = new CollectionInventoryProcessor(config.refsCfg.primaryOnly);
        this.counter = counter;
        
        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor(config);
        refExtractor = new InternalReferenceExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
        searchExtractor = new SearchMetadataExtractor();
        fileDataExtractor = new FileMetadataExtractor(config);
        xpathExtractor = new XPathExtractor();
        
        bundleExtractor = new BundleMetadataExtractor();
        collectionExtractor = new CollectionMetadataExtractor();
    }
    
    
    /**
     * Inner class used by File.find() to select XML PDS label files
     * @author karpenko
     */
    private static class FileMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml"));
        }
    }
    
    
    /**
     * Process a directory
     * @param dir Directory with PDS4 labels
     * @throws Exception Generic exception
     */
    public void processDirectory(File dir) throws Exception
    {
        Stream<Path> stream = null;
        
        try
        {
            stream = Files.find(dir.toPath(), Integer.MAX_VALUE, new FileMatcher());
            Iterator<Path> it = stream.iterator();
            
            while(it.hasNext())
            {
                onFile(it.next().toFile());
            }
        }
        finally
        {
            CloseUtils.close(stream);
        }
    }

    
    /**
     * Process a manifest (file list) file
     * @param manifest A file with a list of full paths to PDS4 labels
     * @throws Exception Generic exception
     */
    public void processManifest(File manifest) throws Exception
    {
        BufferedReader rd = null;
        
        try
        {
            rd = new BufferedReader(new FileReader(manifest));
            
            String line;
            while((line = rd.readLine()) != null)
            {
                if(line.length() == 0 || line.startsWith("#")) continue;
                File file = new File(line.trim());
                onFile(file);
            }
        }
        finally
        {
            CloseUtils.close(rd);
        }
    }

    
    /**
     * Process one file
     * @param file PDS label XML file
     * @throws Exception Generic exception
     */
    private void onFile(File file) throws Exception
    {
        // Skip very large files
        if(file.length() > MAX_XML_FILE_LENGTH)
        {
            log.warn("File is too big to parse: " + file.getAbsolutePath());
            return;
        }

        Document doc = XmlDomUtils.readXml(dbf, file);
        String rootElement = doc.getDocumentElement().getNodeName();
        
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
     * Extract metadata from a label file
     * @param file PDS label file
     * @param doc Parsed XML DOM model of the PDS label file  
     * @throws Exception Generic exception
     */
    private void processMetadata(File file, Document doc) throws Exception
    {
        // Extract basic metadata
        Metadata meta = basicExtractor.extract(file, doc);

        log.info("Processing " + file.getAbsolutePath());

        String rootElement = doc.getDocumentElement().getNodeName();

        // Process Collection specific data
        if("Product_Collection".equals(rootElement))
        {
            processInventoryFiles(file, doc, meta);
        }
        // Process Bundle specific data
        else if("Product_Bundle".equals(rootElement))
        {
            addCollectionRefs(meta, doc);
        }
        // Process supplemental products
        else if("Product_Metadata_Supplemental".equals(rootElement))
        {
            SupplementalWriter swriter = WriterManager.getInstance().getSupplementalWriter();
            swriter.write(file);
        }
        
        // Internal references
        refExtractor.addRefs(meta.intRefs, doc);
        
        // Extract fields by XPath
        xpathExtractor.extract(doc, meta.fields);

        // Extract fields autogenerated from data dictionary
        XmlNamespaces nsInfo = autogenExtractor.extract(file, meta.fields);
        
        // Extract search fields
        searchExtractor.extract(doc, meta.fields);

        // Extract file data
        fileDataExtractor.extract(file, meta);
        
        RegistryDocWriter writer = WriterManager.getInstance().getRegistryWriter();
        writer.write(meta, nsInfo);
        
        counter.prodCounters.inc(meta.prodClass);
    }

    
    /**
     * Process collection inventory files
     * @param collectionFile PDS4 collection label file
     * @param doc Parsed PDS4 collection label file.
     * @param meta Collection metadata extracted from PDS4 collection label file
     * @throws Exception Generic exception
     */
    private void processInventoryFiles(File collectionFile, Document doc, Metadata meta) throws Exception
    {
        Set<String> fileNames = collectionExtractor.extractInventoryFileNames(doc);
        if(fileNames == null) return;
        
        for(String fileName: fileNames)
        {
            File invFile = new File(collectionFile.getParentFile(), fileName);
            invProc.writeCollectionInventory(meta, invFile, false);
        }
    }

    
    private void addCollectionRefs(Metadata meta, Document doc) throws Exception
    {
        List<BundleMetadataExtractor.BundleMemberEntry> bmes = bundleExtractor.extractBundleMemberEntries(doc);

        for(BundleMetadataExtractor.BundleMemberEntry bme: bmes)
        {
            if(!bme.isPrimary && config.refsCfg.primaryOnly) continue;
            
            bundleExtractor.addRefs(meta.intRefs, bme);
        }
    }

}
