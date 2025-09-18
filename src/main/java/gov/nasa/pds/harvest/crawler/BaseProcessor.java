package gov.nasa.pds.harvest.crawler;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.dao.MetadataWriter;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.registry.common.es.service.MissingFieldsProcessor;
import gov.nasa.pds.registry.common.meta.BasicMetadataExtractor;
import gov.nasa.pds.registry.common.meta.FileMetadataExtractor;
import gov.nasa.pds.registry.common.meta.InternalReferenceExtractor;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.meta.MetadataNormalizer;


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
    protected FileMetadataExtractor fileDataExtractor;
    protected InternalReferenceExtractor refExtractor;
    
    private MissingFieldsProcessor mfProc;
    protected MetadataNormalizer metaNormalizer;

    protected String jobId;
    final protected String archive_status;

    /**
     * Constructor.
     * @param config Harvest configuration parameters
     * @throws Exception Generic exception
     */
    public BaseProcessor(HarvestConfigurationType config, String archive_status) throws Exception
    {
        this.archive_status = archive_status;
        this.config = config;

        log = LogManager.getLogger(this.getClass());
        
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        basicExtractor = new BasicMetadataExtractor();
        refExtractor = new InternalReferenceExtractor();        
        fileDataExtractor = new FileMetadataExtractor();
        fileDataExtractor.setProcessDataFiles(config.getFileInfo().isProcessDataFiles());
        fileDataExtractor.setStoreLabels(config.getFileInfo().isStoreLabels(), config.getFileInfo().isStoreJsonLabels());
        
        // Services
        RegistryManager mgr = RegistryManager.getInstance();        
        mfProc = mgr.createMissingFieldsProcessor();
        metaNormalizer = mgr.createMetadataNormalizer();
        
        jobId = PackageIdGenerator.getInstance().getPackageId();
    }

    
    protected void save(Metadata meta) throws Exception
    {
        // Process missing fields
        mfProc.processDoc(meta);        
        MetadataWriter writer = RegistryManager.getInstance().getRegistryWriter();
        writer.write(meta);
    }
    
}
