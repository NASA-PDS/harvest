package gov.nasa.pds.harvest.meta;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.pds.harvest.util.xml.XPathCache;


/**
 * A singleton. Manages references to a common cache and object caches.
 * @author karpenko
 */
public class XPathCacheManager
{
    private static XPathCacheManager instance = new XPathCacheManager();

    private XPathCache commonCache;
    private Map<String, XPathCache> cacheMap;
    

    /**
     * Private constructor. Use getInstance() instead.
     */
    private XPathCacheManager()
    {
        cacheMap = new HashMap<>();
    }


    /**
     * Get singleton instance.
     * @return a singleton
     */
    public static XPathCacheManager getInstance()
    {
        return instance;
    }


    /**
     * Get common cache.
     * @return a common XPath ache.
     */
    public XPathCache getCommonCache()
    {
        return commonCache;
    }


    /**
     * Get a cache by object type, e.g., "Product_Collection"
     * @param type object type
     * @return XPath cache
     */
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
