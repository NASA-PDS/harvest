package gov.nasa.pds.harvest.util.out;

import java.util.Collection;
import com.google.gson.stream.JsonWriter;


public class EsDocUtils
{
    private static final String REPLACE_DOT_WITH = "/";
    
    
    public static void writeField(JsonWriter jw, String key, String value) throws Exception
    {
        if(value == null) return;
        
        key = toEsFieldName(key);
        jw.name(key).value(value);
    }


    public static void writeField(JsonWriter jw, String key, long value) throws Exception
    {
        key = toEsFieldName(key);
        jw.name(key).value(value);
    }

    
    public static void writeField(JsonWriter jw, String key, Collection<String> values) throws Exception
    {
        if(values == null || values.isEmpty()) return;

        key = toEsFieldName(key);
        jw.name(key);

        if(values.size() == 1)
        {
            jw.value(values.iterator().next());
        }
        else
        {
            jw.beginArray();
            for(String value: values)
            {
                jw.value(value);
            }
            jw.endArray();
        }
    }

    
    public static String toEsFieldName(String fieldName)
    {
        return fieldName.replaceAll("\\.", REPLACE_DOT_WITH);
    }
}
