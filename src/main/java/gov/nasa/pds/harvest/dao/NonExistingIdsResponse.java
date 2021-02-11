package gov.nasa.pds.harvest.dao;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.registry.common.es.client.SearchResponseParser;


public class NonExistingIdsResponse implements SearchResponseParser.Callback
{
    private Set<String> retIds;

    
    public NonExistingIdsResponse(Collection<String> ids)
    {
        retIds = new TreeSet<>(ids);
    }
    
    
    public Collection<String> getIds()
    {
        return retIds;
    }
    
    
    @Override
    public void onRecord(String id, Object src) throws Exception
    {
        retIds.remove(id);
    }
}
