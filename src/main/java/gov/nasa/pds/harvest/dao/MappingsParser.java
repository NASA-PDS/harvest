package gov.nasa.pds.harvest.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;


/**
 * Parse Elasticsearch response from "/indexName/_mappings" API
 * to extract field names.
 * 
 * @author karpenko
 */
public class MappingsParser
{
    private String indexName;
    private JsonReader rd;
    private Set<String> fields;
    
    
    /**
     * Constructor
     * @param indexName Elasticsearch index name
     */
    public MappingsParser(String indexName)
    {
        this.indexName = indexName;
    }

    
    /**
     * Parse Elasticsearch response from "/indexName/_mappings" API.
     * @param entity HTTP response body
     * @return a collection of field names from a given Elasticsearch index.
     * @throws IOException an exception
     */
    public Set<String> parse(HttpEntity entity) throws IOException
    {
        InputStream is = entity.getContent();
        rd = new JsonReader(new InputStreamReader(is));
        
        fields = new TreeSet<>();
        
        rd.beginObject();
        
        while(rd.hasNext() && rd.peek() != JsonToken.END_OBJECT)
        {
            // Usually there is only one root element = index name.
            String name = rd.nextName();
            if(indexName.equals(name))
            {
                parseMappings();
            }
            // Usually we should not go here.
            else
            {
                rd.skipValue();
            }
        }
        
        rd.endObject();
        
        rd.close();
        
        return fields;
    }
    

    /**
     * Parse "mappings" JSON object.
     * @throws IOException an exception
     */
    private void parseMappings() throws IOException
    {
        rd.beginObject();
        
        while(rd.hasNext() && rd.peek() != JsonToken.END_OBJECT)
        {
            String name = rd.nextName();
            if("mappings".equals(name))
            {
                parseProps();
            }
            else
            {
                rd.skipValue();
            }
        }
        
        rd.endObject();
    }

    
    /**
     * Parse "properties" JSON object 
     * @throws IOException an exception
     */
    private void parseProps() throws IOException
    {
        rd.beginObject();
        
        while(rd.hasNext() && rd.peek() != JsonToken.END_OBJECT)
        {
            String name = rd.nextName();
            if("properties".equals(name))
            {
                parseFields();
            }
            else
            {
                rd.skipValue();
            }
        }
        
        rd.endObject();
    }

    
    /**
     * Parse fields (indexName -&gt; mappings -&gt; properties -&gt; fields)
     * @throws IOException an exception
     */
    private void parseFields() throws IOException
    {
        rd.beginObject();
        
        while(rd.hasNext() && rd.peek() != JsonToken.END_OBJECT)
        {
            String name = rd.nextName();
            rd.skipValue();
            fields.add(name);
        }
        
        rd.endObject();
    }
    
}
