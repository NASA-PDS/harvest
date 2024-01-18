package gov.nasa.pds.harvest.crawler;

import java.util.HashSet;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.dao.MetadataWriter;
import gov.nasa.pds.harvest.meta.XPathExtractor;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.registry.common.es.service.MissingFieldsProcessor;
import gov.nasa.pds.registry.common.meta.AutogenExtractor;
import gov.nasa.pds.registry.common.meta.BasicMetadataExtractor;
import gov.nasa.pds.registry.common.meta.FileMetadataExtractor;
import gov.nasa.pds.registry.common.meta.InternalReferenceExtractor;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.meta.MetadataNormalizer;
import gov.nasa.pds.registry.common.meta.SearchMetadataExtractor;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;


/**
 * Base class to process PDS4 XML label files
 * @author karpenko
 */
public class BaseProcessor
{
    // Skip files bigger than 10MB
    protected static final long MAX_XML_FILE_LENGTH = 10_000_000;

    protected Logger log;
    
    protected HarvestConfigurationType config;
    protected DocumentBuilderFactory dbf;

    protected BasicMetadataExtractor basicExtractor;
    protected AutogenExtractor autogenExtractor;
    protected FileMetadataExtractor fileDataExtractor;
    protected InternalReferenceExtractor refExtractor;
    protected SearchMetadataExtractor searchExtractor;
    
    protected XPathExtractor xpathExtractor;
    
    private MissingFieldsProcessor mfProc;
    private MetadataNormalizer metaNormalizer;

    protected String jobId;

    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @throws Exception Generic exception
     */
    public BaseProcessor(HarvestConfigurationType config) throws Exception
    {
        this.config = config;

        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);

        basicExtractor = new BasicMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();
        searchExtractor = new SearchMetadataExtractor();
        xpathExtractor = new XPathExtractor();
        
        autogenExtractor = new AutogenExtractor();
        autogenExtractor.setClassFilters(
            new HashSet<String>(config.getAutogenFields().getClassFilter().getInclude()),
            new HashSet<String>(config.getAutogenFields().getClassFilter().getExclude()));
        
        fileDataExtractor = new FileMetadataExtractor();
        fileDataExtractor.setProcessDataFiles(config.getFileInfo().isProcessDataFiles());
        fileDataExtractor.setStoreLabels(config.getFileInfo().isStoreLabels(), config.getFileInfo().isStoreJsonLabels());
        
        // Services
        RegistryManager mgr = RegistryManager.getInstance();        
        mfProc = mgr.createMissingFieldsProcessor();
        metaNormalizer = mgr.createMetadataNormalizer();
        
        jobId = PackageIdGenerator.getInstance().getPackageId();
    }

    
    protected void save(Metadata meta, XmlNamespaces nsInfo) throws Exception
    {
        // Process missing fields
        mfProc.processDoc(meta.fields, nsInfo);
        // Fix (normalize) date and boolean field values
        metaNormalizer.normalizeValues(meta.fields);
        
        MetadataWriter writer = RegistryManager.getInstance().getRegistryWriter();
        writer.write(meta);
    }
    
}
