package gov.nasa.pds.harvest.dao;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import com.google.gson.stream.JsonWriter;
import gov.nasa.pds.harvest.exception.InvalidPDS4ProductException;


/**
 * Elasticsearch request / query builder.
 * 
 * @author karpenko
 */
public class EsRequestBuilder
{
    private boolean pretty;

    
    /**
     * Constructor.
     * @param pretty Generate pretty-formatted JSON
     */
    public EsRequestBuilder(boolean pretty)
    {
        this.pretty = pretty;
    }

    
    /**
     * Construcotr
     */
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

    
    /**
     * Create Elasticsearch query to search for product IDs (lidvids)
     * @param ids Collection of product IDs (lidvids)
     * @param pageSize Number of records to return. Usually pageSize = ids.size().
     * @return JSON Elasticsearch request
     * @throws InvalidPDS4ProductException Invalid PDS4 Product Exception
     * @throws IOException IOException
     */
    public String createSearchIdsRequest(Collection<String> ids, int pageSize) throws InvalidPDS4ProductException, IOException {
        if(ids == null || ids.isEmpty()) throw new InvalidPDS4ProductException("Error reading bundle/collection references. " +
                "Verify the bundle/collection is valid prior to loading the data.");
            
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
