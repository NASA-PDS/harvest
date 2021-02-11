package gov.nasa.pds.harvest.crawler;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class LidVidCache
{
    private Set<String> lidvids;
    private Set<String> lids;


    public LidVidCache()
    {
        lidvids = new TreeSet<>();
        lids = new TreeSet<>();
    }

    
    public void clear()
    {
        lidvids.clear();
        lids.clear();
    }

    
    public boolean containsLidVid(String value)
    {
        return lidvids.contains(value);
    }


    public boolean containsLid(String value)
    {
        return lids.contains(value);
    }

    
    public void addLid(String value)
    {
        if(value == null || value.isEmpty()) return;
        lids.add(value);
    }


    public void addLidVid(String value)
    {
        if(value == null || value.isEmpty()) return;
        lidvids.add(value);
    }

    
    public void addLids(Collection<String> values)
    {
        if(values == null || values.isEmpty()) return;
        lids.addAll(values);
    }


    public void addLidVids(Collection<String> values)
    {
        if(values == null || values.isEmpty()) return;
        lidvids.addAll(values);
    }

}
