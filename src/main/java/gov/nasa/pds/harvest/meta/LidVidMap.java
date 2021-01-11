package gov.nasa.pds.harvest.meta;

import java.util.Map;
import java.util.TreeMap;


public class LidVidMap
{
    private Map<String, String> lidVidMap = new TreeMap<>();
    private Map<String, String> lidMap = new TreeMap<>();

    
    public LidVidMap()
    {
    }

    
    public void clear()
    {
        lidVidMap.clear();
        lidMap.clear();
    }

    
    public void mapLidVids(String key, String val)
    {
        lidVidMap.put(key, val);
    }

    
    public void mapLids(String key, String val)
    {
        lidMap.put(key, val);
    }

    
    public String getLidVid(String lidvid)
    {
        return lidVidMap.get(lidvid);
    }


    public String getLid(String lid)
    {
        return lidMap.get(lid);
    }

}
