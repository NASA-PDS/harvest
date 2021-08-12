package gov.nasa.pds.harvest.cfg.model;

import java.util.List;


/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class FileInfoCfg
{
    public static class FileRefCfg
    {
        public String prefix;
        public String replacement;
    }

    public List<FileRefCfg> fileRef;
    
    public boolean processDataFiles = true;
    public boolean storeLabels = true;
    public boolean storeJsonLabels = true;


    public FileInfoCfg()
    {
    }
}
