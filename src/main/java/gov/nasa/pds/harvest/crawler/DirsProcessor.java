package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.Constants;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.BundleMetadataExtractor;
import gov.nasa.pds.harvest.meta.CollectionMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class DirsProcessor
{
    private Logger log;
    
    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private RegistryDocWriter writer;

    private DocumentBuilderFactory dbf;

    // Bundle and Collection extractors & processors
    private BundleMetadataExtractor bundleExtractor;
    private CollectionMetadataExtractor collectionExtractor;
    private CollectionInventoryProcessor invProc;
    
    // Common extractors
    private BasicMetadataExtractor basicExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private Counter counter;
    
    
    public DirsProcessor(Configuration config, RegistryDocWriter writer, 
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
        refExtractor = new InternalReferenceExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
        fileDataExtractor = new FileMetadataExtractor(config);
        xpathExtractor = new XPathExtractor();
        
        bundleExtractor = new BundleMetadataExtractor();
        collectionExtractor = new CollectionMetadataExtractor();
    }
    
    
    private static class FileMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml"));
        }
    }
    
    
    public void process(File dir) throws Exception
    {
        Iterator<Path> it = Files.find(dir.toPath(), Integer.MAX_VALUE, new FileMatcher()).iterator();
        
        while(it.hasNext())
        {
            onFile(it.next().toFile());
        }
    }
    
    
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

    
    private void processMetadata(File file, Document doc) throws Exception
    {
        String rootElement = doc.getDocumentElement().getNodeName();

        // Extract basic metadata
        Metadata meta = basicExtractor.extract(file, doc);
        meta.fields.addValue(Constants.FLD_NODE_NAME, config.nodeName);

        log.info("Processing " + file.getAbsolutePath());

        // Process Collection specific data
        if("Product_Collection".equals(rootElement))
        {
            processInventoryFiles(file, doc, meta);
        }
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
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }

        // Extract file data
        fileDataExtractor.extract(file, meta);
        
        writer.write(meta);
        
        counter.prodCounters.inc(meta.prodClass);
    }

    
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
