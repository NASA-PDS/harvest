package gov.nasa.pds.harvest.util;

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
    
    
    public void addValue(String fieldName, String value)
    {
        if(fieldName == null) throw new IllegalArgumentException("Field name is null");
        if(value == null) return;
        
        Set<String> values = fields.get(fieldName);
        if(values == null) 
        {
            values = new TreeSet<>();
            fields.put(fieldName, values);
        }
        
        values.add(value);
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
