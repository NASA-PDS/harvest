package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;

import gov.nasa.pds.harvest.cfg.policy.model.BlobStorage;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.ReplaceRule;
import gov.nasa.pds.harvest.meta.MetadataExtractor;
import gov.nasa.pds.harvest.meta.RegistryMetadata;
import gov.nasa.pds.harvest.util.FileData;
import gov.nasa.pds.harvest.util.FileDataBuilder;
import gov.nasa.pds.harvest.util.SolrDocWriter;


public class MetadataProcessor
{
    private static final Logger LOG = Logger.getLogger(MetadataProcessor.class.getName());
    
    private SolrDocWriter writer;
    private MetadataExtractor metaExtractor;

    private FileDataBuilder fdBuilder;
    
    private Policy policy;
    private boolean storeBlob;
    
    
    public MetadataProcessor(File outDir, Policy policy) throws Exception
    {
        writer = new SolrDocWriter(outDir);
        
        metaExtractor = new MetadataExtractor();
        fdBuilder = new FileDataBuilder();
        
        this.policy = policy;
        int blobStorageType = (policy.blobStorage == null) ? BlobStorage.NONE : policy.blobStorage.storageType;
        this.storeBlob = (blobStorageType == BlobStorage.EMBEDDED);
    }

    
    public void process(File file) throws Exception
    {
        RegistryMetadata meta = metaExtractor.extract(file);
        if(!validateAndContinue(meta, file)) return;

        setFileRef(meta, file);
        
        FileData fd = fdBuilder.build(file, "application/xml", storeBlob);
        writer.write(fd, meta);
    }
    
    
    public void close() throws Exception
    {
        writer.close();
    }
    
    
    private boolean validateAndContinue(RegistryMetadata meta, File file)
    {
        if(meta.lid == null)
        {
            LOG.severe("Missing logical identifier: " + file.toURI().getPath());
            return false;
        }

        if(meta.vid == null)
        {
            LOG.severe("Missing version id: " + file.toURI().getPath());
            return false;
        }
        
        return true;
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
