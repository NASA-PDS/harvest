package gov.nasa.pds.harvest.cfg.model;

import java.util.List;
import java.util.Set;


public class Directories
{
    public List<String> paths;
    
    // File filter
    public List<String> fileFilterIncludes;
    public List<String> fileFilterExcludes;
    
    // Dir filter
    public List<String> dirFilterExcludes;

    // Product filter
    public Set<String> prodFilterIncludes;
    public Set<String> prodFilterExcludes;
    
    public Directories()
    {        
    }
    
}
