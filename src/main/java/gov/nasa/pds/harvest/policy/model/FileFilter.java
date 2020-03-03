package gov.nasa.pds.harvest.policy.model;

import java.util.ArrayList;
import java.util.List;

public class FileFilter
{
    protected List<String> include;
    protected List<String> exclude;

    
    public FileFilter()
    {
        include = new ArrayList<>();
        exclude = new ArrayList<>();
    }

    
    public void addInclude(String str)
    {
        include.add(str);
    }

    
    public void addExclude(String str)
    {
        exclude.add(str);
    }

    
    public List<String> getInclude()
    {
        return include;
    }

    
    public List<String> getExclude()
    {
        return exclude;
    }

}
