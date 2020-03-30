package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.model.BlobStorage;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.model.ReplaceRule;
import gov.nasa.pds.harvest.meta.MetadataExtractor;
import gov.nasa.pds.harvest.meta.RegistryMetadata;
import gov.nasa.pds.harvest.util.FileData;
import gov.nasa.pds.harvest.util.FileDataBuilder;
import gov.nasa.pds.harvest.util.solr.SolrDocWriter;


public class MetadataProcessor
{
    private Logger LOG;
    
    private SolrDocWriter writer;
    private MetadataExtractor metaExtractor;

    private FileDataBuilder fdBuilder;
    
    private Configuration policy;
    private boolean storeBlob;
    
    
    public MetadataProcessor(File outDir, Configuration policy) throws Exception
    {
        LOG = LogManager.getLogger(getClass());
        
        writer = new SolrDocWriter(outDir);
        
        metaExtractor = new MetadataExtractor();
        fdBuilder = new FileDataBuilder();
        
        this.policy = policy;
        int blobStorageType = (policy.blobStorage == null) ? BlobStorage.NONE : policy.blobStorage.storageType;
        this.storeBlob = (blobStorageType == BlobStorage.EMBEDDED);
    }

    
    public void process(File file, Counter counter) throws Exception
    {
        LOG.info("Processing file " + file.toURI().getPath());

        RegistryMetadata meta = metaExtractor.extract(file);
        validate(meta, file); 

        setFileRef(meta, file);

        FileData fd = fdBuilder.build(file, "application/xml", storeBlob);
        writer.write(fd, meta);
        
        counter.prodCounters.inc(meta.rootElement);
    }
    
    
    public void close() throws Exception
    {
        writer.close();
    }
    
    
    private void validate(RegistryMetadata meta, File file) throws Exception
    {
        if(meta.lid == null || meta.lid.isEmpty())
        {
            throw new Exception("Missing logical identifier: " + file.toURI().getPath());
        }

        if(meta.vid == null || meta.vid.isEmpty())
        {
            throw new Exception("Missing version id: " + file.toURI().getPath());
        }
    }

    
    private void setFileRef(RegistryMetadata meta, File file)
    {
        if(policy.fileRef == null) return;
        String filePath = file.toURI().getPath();
        
        if(policy.fileRef.rules != null)
        {
            for(ReplaceRule rule: policy.fileRef.rules)
            {
                if(rule.prefix == null || rule.replacement == null) continue;
                
                if(filePath.startsWith(rule.prefix))
                {
                    filePath = rule.replacement + filePath.substring(rule.prefix.length());
                    break;
                }
            }
        }
        
        meta.fileRef = filePath;
    }
}
