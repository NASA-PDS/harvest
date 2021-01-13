package gov.nasa.pds.harvest.cfg.model;

import java.util.List;
import java.util.Set;


public class FiltersCfg
{
    public List<String> bundleDirs;
    
    public Set<String> bundleVerFilter;
    public Set<String> collectionVerFilter;
    
    // File filter
    public List<String> fileFilterIncludes;
    public List<String> fileFilterExcludes;
    
    // Dir filter
    public List<String> dirFilterExcludes;

    // Product filter
    public Set<String> prodClassInclude;
    public Set<String> prodClassExclude;
    
    
    public FiltersCfg()
    {        
    }
    
}
