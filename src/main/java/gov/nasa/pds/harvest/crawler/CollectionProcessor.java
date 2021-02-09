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
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


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
    
    private Counter counter;

    
    public CollectionProcessor(Configuration config, RegistryDocWriter writer, 
            RefsDocWriter refsWriter, Counter counter) throws Exception
    {
        this.config = config;
        this.writer = writer;
        this.invProc = new CollectionInventoryProcessor(refsWriter);
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
    

    private static class CollectionMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml") && fileName.contains("collection"));
        }
    }

    
    public void process(BundleCfg bCfg) throws Exception
    {
        File bundleDir = new File(bCfg.dir);
        Iterator<Path> it = Files.find(bundleDir.toPath(), 2, new CollectionMatcher()).iterator();
        
        while(it.hasNext())
        {
            onCollection(it.next().toFile(), bCfg);
        }
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
                invProc.writeCollectionInventory(meta, invFile);
            }
            else
            {
                invProc.cacheNonRegisteredCollectionInventory(meta, invFile);
            }
        }
    }

}
