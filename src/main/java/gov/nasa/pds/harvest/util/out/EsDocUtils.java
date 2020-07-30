package gov.nasa.pds.harvest.util.out;

import java.util.Collection;
import com.google.gson.stream.JsonWriter;


public class EsDocUtils
{
    public static void writeField(JsonWriter jw, String key, String value) throws Exception
    {
        if(value == null) return;
        jw.name(key).value(value);
    }


    public static void writeField(JsonWriter jw, String key, long value) throws Exception
    {
        jw.name(key).value(value);
    }

    
    public static void writeField(JsonWriter jw, String key, Collection<String> values) throws Exception
    {
        if(values == null || values.isEmpty()) return;
        
        jw.name(key);
        
        jw.beginArray();
        for(String value: values)
        {
            jw.value(value);
        }
        jw.endArray();
    }

}
