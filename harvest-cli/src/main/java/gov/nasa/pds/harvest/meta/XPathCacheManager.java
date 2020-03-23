package gov.nasa.pds.harvest.meta;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.pds.harvest.util.xml.XPathCache;

public class XPathCacheManager
{
    private static XPathCacheManager instance = new XPathCacheManager();

    private XPathCache commonCache;
    private Map<String, XPathCache> cacheMap;
    
    
    private XPathCacheManager()
    {
        cacheMap = new HashMap<>();
    }


    public static XPathCacheManager getInstance()
    {
        return instance;
    }

    
    public XPathCache getCommonCache()
    {
        return commonCache;
    }

    
    public XPathCache getCacheByObjectType(String type)
    {
        return (type == null) ? commonCache : cacheMap.get(type);
    }

    
    XPathCache getOrCreate(String type)
    {
        // Common cache
        if(type == null)
        {
            if(commonCache == null) commonCache = new XPathCache();
            return commonCache;
        }
        
        // Cache by object type
        XPathCache cache = cacheMap.get(type);
        if(cache == null)
        {
            cache = new XPathCache();
            cacheMap.put(type, cache);
        }
        
        return cache;
    }
    
}
