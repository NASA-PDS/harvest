package gov.nasa.pds.harvest.policy.model;

import java.util.ArrayList;
import java.util.List;


public class DirectoryFilter
{
    protected List<String> exclude;

    
    public DirectoryFilter()
    {
        exclude = new ArrayList<>();
    }
    
    
    public List<String> getExclude()
    {
        return this.exclude;
    }

}
