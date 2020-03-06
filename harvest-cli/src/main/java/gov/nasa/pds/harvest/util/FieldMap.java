package gov.nasa.pds.harvest.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FieldMap
{
    private Map<String, Set<String>> fields;
    
    
    public FieldMap()
    {
        fields = new TreeMap<>();
    }
    
    
    public int size()
    {
        return fields.size();
    }
    
    
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
    
    
    public void addValue(String fieldName, String value)
    {
        if(value == null) return;
        
        Set<String> values = getOrCreateValues(fieldName);        
        values.add(value);
    }
    

    public void addValues(String fieldName, String[] values)
    {
        if(values == null || values.length == 0) return;
        
        Set<String> set = getOrCreateValues(fieldName);        
        Collections.addAll(set, values);
    }


    public String getFirstValue(String fieldName)
    {
        Set<String> values = getValues(fieldName);
        return (values == null || values.isEmpty()) ? null : values.iterator().next();
    }


    public Set<String> getValues(String fieldName)
    {
        return fields.get(fieldName);
    }

    
    public Set<String> getNames()
    {
        return fields.keySet();
    }
    
}
