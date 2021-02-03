package gov.nasa.pds.harvest.dao;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import com.google.gson.stream.JsonWriter;


public class EsRequestBuilder
{
    private boolean pretty;

    
    public EsRequestBuilder(boolean pretty)
    {
        this.pretty = pretty;
    }

    
    public EsRequestBuilder()
    {
        this(false);
    }

    
    private JsonWriter createJsonWriter(Writer writer)
    {
        JsonWriter jw = new JsonWriter(writer);
        if (pretty)
        {
            jw.setIndent("  ");
        }

        return jw;
    }

    
    public String createSearchIdsRequest(Set<String> ids, int pageSize) throws Exception
    {
        if(ids == null || ids.isEmpty()) throw new Exception("Missing ids");
            
        StringWriter out = new StringWriter();
        JsonWriter writer = createJsonWriter(out);

        // Create ids query
        writer.beginObject();

        // Exclude source from response
        writer.name("_source").value(false);
        writer.name("size").value(pageSize);

        writer.name("query");
        writer.beginObject();
        writer.name("ids");
        writer.beginObject();
        
        writer.name("values");
        writer.beginArray();
        for(String id: ids)
        {
            writer.value(id);
        }
        writer.endArray();
        
        writer.endObject();
        writer.endObject();
        writer.endObject();

        writer.close();
        return out.toString();
    }

}
