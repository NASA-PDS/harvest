package gov.nasa.pds.harvest.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.util.out.SupplementalWriter;
import gov.nasa.pds.harvest.util.out.WriterManager;
import gov.nasa.pds.harvest.util.xml.XmlIs;
import gov.nasa.pds.registry.common.es.service.CollectionInventoryWriter;
import gov.nasa.pds.registry.common.meta.BundleMetadataExtractor;
import gov.nasa.pds.registry.common.meta.CollectionMetadataExtractor;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.CloseUtils;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;


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
public class FilesProcessor extends BaseProcessor
{
    // Bundle and Collection extractors & processors
    private BundleMetadataExtractor bundleExtractor;
    private CollectionMetadataExtractor collectionExtractor;
    
    
    /**
     * Constructor
     * @param config Harvest configuration parameters (from a config file)
     * @throws Exception Generic exception
     */
    public FilesProcessor(HarvestConfigurationType config, String archive_status) throws Exception
    {
        super(config, archive_status);
        
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
            return XmlIs.aLabel(path.toString());
        }
    }
    
    
    /**
     * Process a directory
     * @param dir Directory with PDS4 labels
     * @throws Exception Generic exception
     */
    public void processDirectory(File dir) throws Exception {
      try (Stream<Path> stream = Files.find(
          dir.toPath(),
          Integer.MAX_VALUE,
          new FileMatcher(), FileVisitOption.FOLLOW_LINKS)) {
        Iterator<Path> it = stream.iterator();

        while(it.hasNext()) {
          onFile(it.next().toFile());
        }
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
            log.error(ex.getMessage());
            counter.failedFileCount++;
            return;
        }        
        
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Apply product filter
        if(!config.getProductFilter().getInclude().isEmpty() &&
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
     * Extract metadata from a label file
     * @param file PDS label file
     * @param doc Parsed XML DOM model of the PDS label file  
     * @throws Exception Generic exception
     */
    private void processMetadata(File file, Document doc) throws Exception
    {
        // Extract basic metadata
        Metadata meta = basicExtractor.extract(file, doc, this.archive_status);
        meta.setNodeName(ConfigManager.exchangeIndexForNode(RegistryManager.getInstance().getIndexName()));

        log.info("Processing " + file.getAbsolutePath());

        String rootElement = doc.getDocumentElement().getNodeName();

        // Process Bundle specific data
        if("Product_Bundle".equals(rootElement))
        {
            addCollectionRefs(meta, doc);
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
        fileDataExtractor.extract(file, meta, ConfigManager.exchangeFileRef(config.getFileInfo().getFileRef()));
        
        // Save data
        save(meta, nsInfo);
        
        // Process Collection inventory
        if("Product_Collection".equals(rootElement))
        {
            processInventoryFiles(file, doc, meta);
        }
        // Process supplemental products
        else if("Product_Metadata_Supplemental".equals(rootElement))
        {
            SupplementalWriter swriter = WriterManager.getInstance().getSupplementalWriter();
            swriter.write(file);
        }
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
        
        RegistryManager mgr = RegistryManager.getInstance();
        if(!mgr.isOverwrite())
        {
            // Check if this collection already registered
            RegistryDao regDao = mgr.getRegistryDao();
            if(regDao.idExists(meta.lidvid))
            {
                return;
            }
        }
                
        CollectionInventoryWriter invWriter = mgr.getCollectionInventoryWriter();
        
        for(String fileName: fileNames)
        {
            File invFile = new File(collectionFile.getParentFile(), fileName);
            invWriter.writeCollectionInventory(meta.lidvid, invFile, jobId);
        }
    }

    
    private void addCollectionRefs(Metadata meta, Document doc) throws Exception
    {
        List<BundleMetadataExtractor.BundleMemberEntry> bmes = bundleExtractor.extractBundleMemberEntries(doc);

        for(BundleMetadataExtractor.BundleMemberEntry bme: bmes)
        {
            if(!bme.isPrimary && config.getReferences().isPrimaryOnly()) continue;
            
            bundleExtractor.addRefs(meta.intRefs, bme);
        }
    }

}
