package gov.nasa.pds.harvest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LidVidUtils
{
    public static List<String> lidvidToLid(Collection<String> lidvids)
    {
        List<String> lids = new ArrayList<>();
        
        for(String lidvid: lidvids)
        {
            String lid = lidvidToLid(lidvid);
            lids.add(lid);
        }
        
        return lids;
    }
    
    
    public static String lidvidToLid(String lidvid)
    {
        int idx = lidvid.indexOf("::");
        return lidvid.substring(0, idx);
    }

}
