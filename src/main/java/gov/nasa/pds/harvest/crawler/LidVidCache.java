package gov.nasa.pds.harvest.crawler;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A cache of product ids (lids and lidvids)
 * 
 * @author karpenko
 */
public class LidVidCache
{
    private Set<String> lidvids;
    private Set<String> lids;


    /**
     * Constructor
     */
    public LidVidCache()
    {
        lidvids = new TreeSet<>();
        lids = new TreeSet<>();
    }

    
    /**
     * Clear the cache.
     */
    public void clear()
    {
        lidvids.clear();
        lids.clear();
    }

    
    /**
     * Check if the cache contains a lidvid
     * @param value LidVid
     * @return true if cache contains the LidVid
     */
    public boolean containsLidVid(String value)
    {
        return lidvids.contains(value);
    }

    /**
     * Check if the cache contains a lid
     * @param value Lid
     * @return true if cache contains the lid
     */
    public boolean containsLid(String value)
    {
        return lids.contains(value);
    }

    
    /**
     * Add a lid to the cache
     * @param value lid
     */
    public void addLid(String value)
    {
        if(value == null || value.isEmpty()) return;
        lids.add(value);
    }


    /**
     * Add a lidvid to the cache
     * @param value LidVid
     */
    public void addLidVid(String value)
    {
        if(value == null || value.isEmpty()) return;
        lidvids.add(value);
    }

    
    /**
     * Add multiple lids to the cache
     * @param values Lid collection
     */
    public void addLids(Collection<String> values)
    {
        if(values == null || values.isEmpty()) return;
        lids.addAll(values);
    }

    
    /**
     * Add multiple lidvids to the cache
     * @param values LidVid collection
     */
    public void addLidVids(Collection<String> values)
    {
        if(values == null || values.isEmpty()) return;
        lidvids.addAll(values);
    }

}
