package gov.nasa.pds.harvest.policy.model;

import java.util.List;


public class Directory
{
    protected List<String> path;
    protected FileFilter fileFilter;
    protected DirectoryFilter directoryFilter;
    
    
    public Directory()
    {        
    }
    
    
    public List<String> getPath()
    {
        return path;
    }
    
    
    public FileFilter getFileFilter()
    {
        return fileFilter;
    }

    
    public DirectoryFilter getDirectoryFilter()
    {
        return directoryFilter;
    }

}
