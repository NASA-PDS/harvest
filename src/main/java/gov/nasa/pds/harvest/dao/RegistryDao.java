package gov.nasa.pds.harvest.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import gov.nasa.pds.registry.common.Request;
import gov.nasa.pds.registry.common.Response;
import gov.nasa.pds.registry.common.RestClient;
import gov.nasa.pds.registry.common.util.SearchResponseParser;


/**
 * Elasticsearch "registry" index Data Access Object (DAO).
 * 
 * @author karpenko
 */
public class RegistryDao
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
    public RegistryDao(RestClient client, String indexName)
    {
        this(client, indexName, false);
    }
    
    
    /**
     * Constructor.
     * @param client Elasticsearch client
     * @param indexName Elasticsearch index name, e.g., "registry".
     * @param pretty Pretty-format Elasticsearch response JSON. Used for debugging.
     */
    public RegistryDao(RestClient client, String indexName, boolean pretty)
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
     * @throws Exception Generic exception
     */
    public boolean idExists(String id) throws Exception
    {
        List<String> ids = new ArrayList<>(1);
        ids.add(id);
        
        Collection<String> retIds = getNonExistingIds(ids);
        return retIds.isEmpty();
    }
    
    
    /**
     * Check if given product IDs (lidvids) exist in Elasticsearch "registry" 
     * collection. Return values that don't exist in Elasticsearch.
     * @param ids Search these IDs (lidvids) in Elasticsearch
     * @return a list of product IDs (lidvids) that don't exist in Elasticsearch "registry" collection.
     * @throws Exception Generic exception
     */
    public Set<String> getNonExistingIds(Collection<String> ids) throws Exception
    {
        if(ids == null || ids.isEmpty()) return new HashSet<>();
        Response resp = searchIds(ids);

        NonExistingIdsResponse idsResp = new NonExistingIdsResponse(ids);
        parser.parseResponse(resp, idsResp);

        return idsResp.getIds();
    }
    
    
    private Response searchIds(Collection<String> ids) throws Exception
    {
        String json = requestBld.createSearchIdsRequest(ids, ids.size());
        
        String reqUrl = "/" + indexName + "/_search";
        if(pretty) reqUrl += "?pretty";
        
        Request req = client.createRequest(Request.Method.GET, reqUrl);
        req.setJsonEntity(json);
        Response resp = client.performRequest(req);

        return resp;
    }
    
}
