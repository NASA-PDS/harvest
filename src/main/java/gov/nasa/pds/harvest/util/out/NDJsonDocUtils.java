package gov.nasa.pds.harvest.util.out;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import com.google.gson.stream.JsonWriter;


public class NDJsonDocUtils
{
    private static final String REPLACE_DOT_WITH = "/";


    public static void writePK(Writer writer, String id) throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();
        
        jw.name("index");
        jw.beginObject();
        jw.name("_id").value(id);
        jw.endObject();
        
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
    }

    
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

        jw.beginArray();
        for(String value: values)
        {
            jw.value(value);
        }
        jw.endArray();
    }

    
    public static String toEsFieldName(String fieldName)
    {
        return fieldName.replaceAll("\\.", REPLACE_DOT_WITH);
    }
}
