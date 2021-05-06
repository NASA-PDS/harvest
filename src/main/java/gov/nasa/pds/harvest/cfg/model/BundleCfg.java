package gov.nasa.pds.harvest.cfg.model;

import java.util.Set;

/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class BundleCfg
{
    public String dir;
    public Set<String> versions;
    
    public Set<String> collectionLids;
    public Set<String> collectionLidVids;
    
    public Set<String> productDirs;
}
