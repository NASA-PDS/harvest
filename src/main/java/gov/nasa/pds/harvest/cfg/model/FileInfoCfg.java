package gov.nasa.pds.harvest.cfg.model;

import java.util.List;


public class FileInfoCfg
{
    public static class FileRefCfg
    {
        public String prefix;
        public String replacement;
    }

    public List<FileRefCfg> fileRef;
    
    public boolean processDataFiles = true;
    public boolean storeLabels = false;    

    
    public FileInfoCfg()
    {
    }
}
