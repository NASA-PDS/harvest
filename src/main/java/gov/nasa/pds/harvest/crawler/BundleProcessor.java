package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.dao.RegistryDAO;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.BundleMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class BundleProcessor
{
    private Logger log;

    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private RegistryDocWriter writer;
    
    private DocumentBuilderFactory dbf;
    private BasicMetadataExtractor basicExtractor;
    private BundleMetadataExtractor bundleExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private int bundleCount;
    private Counter counter;

    private BundleCfg bundleCfg;
    
    
    public BundleProcessor(Configuration config, RegistryDocWriter writer, Counter counter) throws Exception
    {
        this.config = config;
        this.writer = writer;
        this.counter = counter;
        
        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor();
        bundleExtractor = new BundleMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();
        xpathExtractor = new XPathExtractor();
        autogenExtractor = new AutogenExtractor(config.autogen);
        fileDataExtractor = new FileMetadataExtractor(config);
    }
    
    
    private static class BundleMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml") && fileName.contains("bundle"));
        }
    }

    
    public int process(BundleCfg bCfg) throws Exception
    {
        bundleCount = 0;
        this.bundleCfg = bCfg;
        
        File bundleDir = new File(bCfg.dir);
        Iterator<Path> it = Files.find(bundleDir.toPath(), 1, new BundleMatcher()).iterator();
        while(it.hasNext())
        {
            onBundle(it.next().toFile());
        }

        return bundleCount;
    }


    private void onBundle(File file) throws Exception
    {
        // Skip very large files
        if(file.length() > MAX_XML_FILE_LENGTH)
        {
            log.warn("File is too big to parse: " + file.getAbsolutePath());
            return;
        }

        Document doc = XmlDomUtils.readXml(dbf, file);
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Ignore non-bundle XMLs
        if(!"Product_Bundle".equals(rootElement)) return;
        
        processMetadata(file, doc);
    }
    
    
    private void processMetadata(File file, Document doc) throws Exception
    {
        Metadata meta = basicExtractor.extract(file, doc);
        if(bundleCfg.versions != null && !bundleCfg.versions.contains(meta.vid)) return;

        log.info("Processing bundle " + file.getAbsolutePath());
        bundleCount++;
        
        RegistryDAO dao = (RegistryManager.getInstance() == null) ? null 
                : RegistryManager.getInstance().getRegistryDAO(); 

        // Bundle already registered in the Registry (Elasticsearch)
        if(dao != null && dao.idExists(meta.lidvid))
        {
            log.warn("Bundle " + meta.lidvid + " already registered");
            addCollectionRefs(meta, doc);
            counter.skippedFileCount++;
            return;
        }
        
        refExtractor.addRefs(meta.intRefs, doc);
        addCollectionRefs(meta, doc);
        xpathExtractor.extract(doc, meta.fields);
        
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }
        
        fileDataExtractor.extract(file, meta);
        
        writer.write(meta);
        
        counter.prodCounters.inc(meta.prodClass);
    }

    
    private void addCollectionRefs(Metadata meta, Document doc) throws Exception
    {
        List<BundleMetadataExtractor.BundleMemberEntry> bmes = bundleExtractor.extractBundleMemberEntries(doc);

        for(BundleMetadataExtractor.BundleMemberEntry bme: bmes)
        {
            if(!bme.isPrimary && config.refsCfg.primaryOnly) continue;
            
            cacheRefs(bme);
            bundleExtractor.addRefs(meta.intRefs, bme);
        }
    }
    

    private void cacheRefs(BundleMetadataExtractor.BundleMemberEntry bme)
    {
        // Only cache primary references
        if(!bme.isPrimary) return;
        
        LidVidCache cache = RefsCache.getInstance().getCollectionRefsCache();

        if(bme.lidvid != null) cache.addLidVid(bme.lidvid);
        if(bme.lid != null) cache.addLid(bme.lid);
    }

}
