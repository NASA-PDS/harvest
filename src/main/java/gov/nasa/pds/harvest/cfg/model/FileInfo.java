package gov.nasa.pds.harvest.cfg.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class FileInfo
{
    public static final int BLOB_NONE = 0;
    public static final int BLOB_EMBEDDED = 1;
    
    public int blobStorageType;

    private Logger LOG;
    
    
    public FileInfo()
    {
        LOG = LogManager.getLogger(getClass());
        blobStorageType = BLOB_NONE;
    }
    
    
    public void setBlobStorageType(String type)
    {
        if(type == null || type.equalsIgnoreCase("none"))
        {
            blobStorageType = FileInfo.BLOB_NONE;
        }
        else if(type.equalsIgnoreCase("embedded"))
        {
            blobStorageType = FileInfo.BLOB_EMBEDDED;
        }
        else
        {
            blobStorageType = FileInfo.BLOB_NONE;
            LOG.warn("Unknown blob storage type " + type);
        }
        
    }
}
