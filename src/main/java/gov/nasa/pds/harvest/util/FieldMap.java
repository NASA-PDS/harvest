package gov.nasa.pds.harvest.util;

import java.util.Collection;
import java.util.Set;

public interface FieldMap
{
    public void addValue(String fieldName, String value);
    public void addValues(String fieldName, String[] values);

    public Collection<String> getValues(String fieldName);
    public String getFirstValue(String fieldName);
    public Set<String> getNames();
    
    public boolean isEmpty();
    public int size();
}
