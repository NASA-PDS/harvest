package gov.nasa.pds.harvest.util;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;


/**
 * Helper methods to work with product IDs (LIDs and LIDVIDs).
 * 
 * @author karpenko
 */
public class LidVidUtils
{
    /**
     * Convert lidvids to lids. Discard vid (version) information.
     * @param lidvids
     * @return
     */
    public static Set<String> lidvidToLid(Collection<String> lidvids)
    {
        if(isEmpty(lidvids)) return null;
        
        Set<String> lids = new TreeSet<>();
        
        for(String lidvid: lidvids)
        {
            String lid = lidvidToLid(lidvid);
            lids.add(lid);
        }
        
        return lids;
    }
    
    
    /**
     * Convert lidvid to lid. Discard vid (version) information.
     * @param lidvid
     * @return
     */
    public static String lidvidToLid(String lidvid)
    {
        int idx = lidvid.indexOf("::");
        return lidvid.substring(0, idx);
    }

    
    /**
     * Add all values from a collection to the set. 
     * @param set
     * @param col
     * @return
     */
    public static Set<String> add(Set<String> set, Collection<String> col)
    {
        if(isEmpty(col)) return set;
        
        if(set == null) set = new TreeSet<>();
        set.addAll(col);
        
        return set;
    }
    
    
    private static boolean isEmpty(Collection<String> col)
    {
        return col == null || col.isEmpty();
    }
}
