package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.function.BiPredicate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.meta.BasicMetadataExtractor;
import gov.nasa.pds.harvest.meta.FileMetadataExtractor;
import gov.nasa.pds.harvest.meta.InternalReferenceExtractor;
import gov.nasa.pds.harvest.meta.LidVidMap;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.out.DocWriter;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class CollectionProcessor
{
    private Logger log;

    // Skip files bigger than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;

    private Configuration config;
    private DocWriter writer;
    
    private DocumentBuilderFactory dbf;
    private BasicMetadataExtractor basicExtractor;
    private InternalReferenceExtractor refExtractor;
    private AutogenExtractor autogenExtractor;
    private XPathExtractor xpathExtractor;
    private FileMetadataExtractor fileDataExtractor;
    
    private int collectionCount;

    
    public CollectionProcessor(Configuration config, DocWriter writer) throws Exception
    {
        this.config = config;
        this.writer = writer;
        
        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        
        basicExtractor = new BasicMetadataExtractor();
        refExtractor = new InternalReferenceExtractor(config.internalRefs);
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

    
    public void process(File bundleDir, LidVidMap colToBundleMap) throws Exception
    {
        log.info("Processing collections...");
        
        Iterator<Path> it = Files.find(bundleDir.toPath(), 2, new CollectionMatcher()).iterator();
        
        while(it.hasNext())
        {
            onCollection(it.next().toFile(), colToBundleMap);
        }
    }


    private void onCollection(File file, LidVidMap colToBundleMap) throws Exception
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
        
        log.info("Processing collection " + file.getAbsolutePath());
        collectionCount++;
        
        processMetadata(file, doc, colToBundleMap);
    }
    
    
    private void processMetadata(File file, Document doc, LidVidMap colToBundleMap) throws Exception
    {
        Metadata meta = basicExtractor.extract(doc);
        refExtractor.addRefs(meta.intRefs, doc);
        addBundleRefs(meta, colToBundleMap);
        xpathExtractor.extract(doc, meta.fields);
        
        if(config.autogen != null)
        {
            autogenExtractor.extract(file, meta.fields);
        }
        
        fileDataExtractor.extract(file, meta);
        
        writer.write(meta);
    }
    
    
    private void addBundleRefs(Metadata meta, LidVidMap colToBundleMap)
    {
        String val = colToBundleMap.getLidVid(meta.lidvid);
        if(val != null)
        {
            String key = "ref_lidvid_bundle";
            meta.intRefs.addValue(key, val);
        }

        val = colToBundleMap.getLid(meta.lid);
        if(val != null)
        {
            String key = "ref_lid_bundle";
            meta.intRefs.addValue(key, val);
        }
    }
}
