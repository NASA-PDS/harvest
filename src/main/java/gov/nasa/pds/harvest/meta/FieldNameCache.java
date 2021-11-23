package gov.nasa.pds.harvest.meta;

import java.util.Set;
import java.util.TreeSet;

/**
 * A cache of field names in Elasticsearch schema for the registry index.
 * Implemented as a singleton.
 * @author karpenko
 */
public class FieldNameCache
{
    private static FieldNameCache singleton = new FieldNameCache();
    private Set<String> fieldNames;
    
    
    /**
     * Private constructor. Use getInstance() instead.
     */
    private FieldNameCache()
    {
        fieldNames = new TreeSet<>();
    }

    
    /**
     * Get the singleton instance 
     * @return field cache instance
     */
    public static FieldNameCache getInstance()
    {
        return singleton;
    }
    
    
    /**
     * Set cached values
     * @param fieldNames
     */
    public void set(Set<String> fieldNames)
    {
        this.fieldNames = fieldNames;
    }
    
    
    /**
     * Check if a name is in the cache.
     * @param name field name
     * @return true if field name is cached.
     */
    public boolean containsName(String name)
    {
        return fieldNames.contains(name);
    }
}
