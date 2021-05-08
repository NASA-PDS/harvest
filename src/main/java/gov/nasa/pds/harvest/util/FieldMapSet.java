package gov.nasa.pds.harvest.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * Implementation of FieldMap interface which stores values in a set.
 * Order and number of values in PDS label XML are not preserved.
 * Only unique values are stored. Values are sorted alphabetically (as a string).
 * 
 * @author karpenko
 */
public class FieldMapSet implements FieldMap
{
    private Map<String, Set<String>> fields;
        
    /**
     * Constructor
     */
    public FieldMapSet()
    {
        fields = new TreeMap<>();
    }
    
    
    /**
     * Map size / number of fields.
     */
    public int size()
    {
        return fields.size();
    }
    
    
    /**
     * Check if map is empty.
     */
    public boolean isEmpty()
    {
        return fields.size() == 0;
    }
    
    
    private Set<String> getOrCreateValues(String fieldName)
    {
        if(fieldName == null) throw new IllegalArgumentException("Field name is null");

        Set<String> values = fields.get(fieldName);
        if(values == null) 
        {
            values = new TreeSet<>();
            fields.put(fieldName, values);
        }

        return values;
    }
    
    
    /**
     * Add field's value.
     */
    public void addValue(String fieldName, String value)
    {
        if(value == null) return;
        
        Set<String> values = getOrCreateValues(fieldName);        
        values.add(value);
    }
    

    /**
     * Add multiple values.
     */
    public void addValues(String fieldName, String[] values)
    {
        if(values == null || values.length == 0) return;
        
        Set<String> set = getOrCreateValues(fieldName);        
        Collections.addAll(set, values);
    }


    /**
     * Get first value of a field.
     */
    public String getFirstValue(String fieldName)
    {
        Collection<String> values = getValues(fieldName);
        return (values == null || values.isEmpty()) ? null : values.iterator().next();
    }


    /**
     * Get all values of a field.
     */
    public Collection<String> getValues(String fieldName)
    {
        return fields.get(fieldName);
    }

    
    /**
     * Get names of all fields in this map.
     */
    public Set<String> getNames()
    {
        return fields.keySet();
    }
    
}
