package gov.nasa.pds.harvest.meta;

import java.util.HashMap;
import java.util.Map;

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
        return cacheMap.get(type);
    }

    
    XPathCache getOrCreate(String type)
    {
        XPathCache cache = cacheMap.get(type);
        if(cache == null)
        {
            cache = new XPathCache();
            cacheMap.put(type, cache);
        }
        
        return cache;
    }
    
}
