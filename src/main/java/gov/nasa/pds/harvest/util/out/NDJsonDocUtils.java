package gov.nasa.pds.harvest.util.out;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import com.google.gson.stream.JsonWriter;


/**
 * Utility class to write NJSON (new-line delimited JSON) documents.
 *  
 * @author karpenko
 *
 */
public class NDJsonDocUtils
{
    private static final String REPLACE_DOT_WITH = "/";


    /**
     * Write primary key (first line in NJSON record)
     * @param writer
     * @param id
     * @throws Exception
     */
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

    
    /**
     * Write a field.
     * @param jw
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(JsonWriter jw, String key, String value) throws Exception
    {
        if(value == null) return;
        
        key = toEsFieldName(key);
        jw.name(key).value(value);
    }


    /**
     * Write a field.
     * @param jw
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(JsonWriter jw, String key, long value) throws Exception
    {
        key = toEsFieldName(key);
        jw.name(key).value(value);
    }

    
    /**
     * Write a field.
     * @param jw
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(JsonWriter jw, String key, float value) throws Exception
    {
        key = toEsFieldName(key);
        jw.name(key).value(value);
    }

    
    /**
     * Write a field.
     * @param jw
     * @param key
     * @param values
     * @throws Exception
     */
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

    
    /**
     * Convert registry field name to the valid Elasticsearch field name. 
     * (Replace '.' with '/').
     * @param fieldName
     * @return
     */
    public static String toEsFieldName(String fieldName)
    {
        return fieldName.replaceAll("\\.", REPLACE_DOT_WITH);
    }
}
