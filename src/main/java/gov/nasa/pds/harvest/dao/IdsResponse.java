package gov.nasa.pds.harvest.dao;

import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.registry.common.es.client.SearchResponseParser;


public class IdsResponse implements SearchResponseParser.Callback
{
    private Set<String> ids;
    
    public IdsResponse()
    {
        ids = new TreeSet<>();
    }
    
    public Set<String> getIds()
    {
        return ids;
    }
    
    @Override
    public void onRecord(String id, Object src) throws Exception
    {
        ids.add(id);
    }
}
