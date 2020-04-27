package gov.nasa.pds.harvest.util.xml;

import java.util.HashMap;
import java.util.Map;


public class NsUriToPrefixMap
{
    private static NsUriToPrefixMap instance = new NsUriToPrefixMap();
    
    private Map<String, String> map;
    
    
    private NsUriToPrefixMap()
    {
        map = new HashMap<>();
    }

    
    public static NsUriToPrefixMap getInstance()
    {
        return instance;
    }
    
    
    public String getPrefixByUri(String uri)
    {
        return map.get(uri);
    }


    public void add(String uri, String prefix)
    {
        map.put(uri, prefix);
    }
}
