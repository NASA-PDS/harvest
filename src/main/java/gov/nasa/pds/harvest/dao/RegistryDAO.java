package gov.nasa.pds.harvest.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import gov.nasa.pds.registry.common.es.client.SearchResponseParser;


public class RegistryDAO
{
    private RestClient client;
    private String indexName;
    private boolean pretty;

    private EsRequestBuilder requestBld;
    private SearchResponseParser parser;
    
    
    public RegistryDAO(RestClient client, String indexName)
    {
        this(client, indexName, false);
    }
    
    
    public RegistryDAO(RestClient client, String indexName, boolean pretty)
    {
        this.client = client;
        this.indexName = indexName;
        this.pretty = pretty;
        
        requestBld = new EsRequestBuilder();
        parser = new SearchResponseParser();
    }

    
    public boolean idExists(String id) throws Exception
    {
        List<String> ids = new ArrayList<>(1);
        ids.add(id);
        
        Response resp = searchIds(ids, 1);
        IdsResponse idsResp = new IdsResponse();
        parser.parseResponse(resp, idsResp);

        return idsResp.getIds().size() == 1;
    }
    
    
    public List<String> getNonExistingIds(Set<String> ids, int pageSize) throws Exception
    {
        Response resp = searchIds(ids, pageSize);

        IdsResponse idsResp = new IdsResponse();
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
