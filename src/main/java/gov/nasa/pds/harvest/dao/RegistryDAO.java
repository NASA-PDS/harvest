package gov.nasa.pds.harvest.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import gov.nasa.pds.registry.common.es.client.SearchResponseParser;


/**
 * Elasticsearch "registry" index Data Access Object (DAO).
 * 
 * @author karpenko
 */
public class RegistryDAO
{
    private RestClient client;
    private String indexName;
    private boolean pretty;

    private EsRequestBuilder requestBld;
    private SearchResponseParser parser;
    
    
    /**
     * Constructor
     * @param client Elasticsearch client
     * @param indexName Elasticsearch index name, e.g., "registry".
     */
    public RegistryDAO(RestClient client, String indexName)
    {
        this(client, indexName, false);
    }
    
    
    /**
     * Constructor.
     * @param client Elasticsearch client
     * @param indexName Elasticsearch index name, e.g., "registry".
     * @param pretty Pretty-format Elasticsearch response JSON. Used for debugging.
     */
    public RegistryDAO(RestClient client, String indexName, boolean pretty)
    {
        this.client = client;
        this.indexName = indexName;
        this.pretty = pretty;
        
        requestBld = new EsRequestBuilder();
        parser = new SearchResponseParser();
    }

    
    /**
     * Check if product id (lidvid) exists in "registry" index in Elasticsearch.
     * @param id Product ID (lidvid)
     * @return true if product exists
     * @throws Exception
     */
    public boolean idExists(String id) throws Exception
    {
        List<String> ids = new ArrayList<>(1);
        ids.add(id);
        
        Collection<String> retIds = getNonExistingIds(ids, 1);
        return retIds.isEmpty();
    }
    
    
    /**
     * Check if given product IDs (lidvids) exist in Elasticsearch "registry" 
     * collection. Return values that don't exist in Elasticsearch.
     * @param ids Search these IDs (lidvids) in Elasticsearch
     * @param pageSize max number of results to return. Usually pageSize = ids.size()
     * @return a list of product IDs (lidvids) that don't exist in Elasticsearch "registry" collection.
     * @throws Exception
     */
    public Collection<String> getNonExistingIds(Collection<String> ids, int pageSize) throws Exception
    {
        Response resp = searchIds(ids, pageSize);

        NonExistingIdsResponse idsResp = new NonExistingIdsResponse(ids);
        parser.parseResponse(resp, idsResp);

        return idsResp.getIds();
    }
    
    
    private Response searchIds(Collection<String> ids, int pageSize) throws Exception
    {
        if(pageSize < ids.size()) throw new IllegalArgumentException("Page size is less than ids size");

        String json = requestBld.createSearchIdsRequest(ids, pageSize);
        
        String reqUrl = "/" + indexName + "/_search";
        if(pretty) reqUrl += "?pretty";
        
        Request req = new Request("GET", reqUrl);
        req.setJsonEntity(json);
        Response resp = client.performRequest(req);

        return resp;
    }
    
}
