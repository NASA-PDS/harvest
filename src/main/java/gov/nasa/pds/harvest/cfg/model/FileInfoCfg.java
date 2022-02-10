package gov.nasa.pds.harvest.cfg.model;

import java.util.List;

import gov.nasa.pds.registry.common.meta.cfg.FileRefRule;


/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class FileInfoCfg
{
    public List<FileRefRule> fileRef;
    
    public boolean processDataFiles = true;
    public boolean storeLabels = true;
    public boolean storeJsonLabels = true;


    public FileInfoCfg()
    {
    }
}
