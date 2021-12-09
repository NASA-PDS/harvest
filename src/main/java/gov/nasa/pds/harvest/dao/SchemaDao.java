package gov.nasa.pds.harvest.dao;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

/**
 * Elasticsearch schema DAO (Data Access Object).
 * This class provides methods to read and update Elasticsearch schema
 * and data dictionary. 
 * 
 * @author karpenko
 */
public class SchemaDao
{
    private Logger log;
    private RestClient client;
    private String indexName;
    
    /**
     * Constructor
     * @param client Elasticsearch client
     * @param indexName Elasticsearch index name
     */
    public SchemaDao(RestClient client, String indexName)
    {
        log = LogManager.getLogger(this.getClass());
        this.client = client;
        this.indexName = indexName;
    }
    
    
    /**
     * Call Elasticsearch "mappings" API to get a list of field names.
     * @return a collection of field names
     * @throws Exception an exception
     */
    public Set<String> getFieldNames() throws Exception
    {
        Request req = new Request("GET", "/" + indexName + "/_mappings");
        Response resp = client.performRequest(req);
        
        MappingsParser parser = new MappingsParser(indexName);
        return parser.parse(resp.getEntity());
    }
}
