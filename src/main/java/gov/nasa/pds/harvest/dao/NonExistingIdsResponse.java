package gov.nasa.pds.harvest.dao;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.registry.common.es.client.SearchResponseParser;


/**
 * Helper class to process Elasticsearch response from "search IDs" query.  
 * 
 * @author karpenko
 */
public class NonExistingIdsResponse implements SearchResponseParser.Callback
{
    private Set<String> retIds;


    /**
     * Constructor
     * @param ids Product IDs (lidvids) sent to Elasticsearch in "search IDs" query.
     * IDs are copied to internal collection.
     * After processing Elasticsearch response, all IDs existing in Elasticsearch
     * "registry" index will be removed from this internal collection.
     */
    public NonExistingIdsResponse(Collection<String> ids)
    {
        retIds = new TreeSet<>(ids);
    }
    
    /**
     * Return collection of product IDs (lidvids) non-existing in Elasticsearch.
     * @return a collection of product IDs (lidvids)
     */
    public Set<String> getIds()
    {
        return retIds;
    }
    
    
    /**
     * This method is called for each record in Elasticsearch response
     */
    @Override
    public void onRecord(String id, Object src) throws Exception
    {
        retIds.remove(id);
    }
}
