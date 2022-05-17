package gov.nasa.pds.harvest.dao;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.json.RegistryDocBuilder;

/**
 * A batch of NJSON documents to be loaded into Elasticsearch
 * @author karpenko
 */
public class RegistryDocBatch
{
    public static class NJsonItem
    {
        public String lidvid;
        public String prodClass;    // Product class is required to update product counter
        public String pkJson;       // Primary key JSON (line 1)
        public String dataJson;     // Data JSON (line 2)

    }
    
    private List<NJsonItem> items;
    
    
    /**
     * Constructor
     */
    public RegistryDocBatch()
    {
        items = new ArrayList<>();
    }

    
    public void write(Metadata meta, String jobId) throws Exception
    {
        NJsonItem item = new NJsonItem();
        item.lidvid = meta.lidvid;
        item.prodClass = meta.prodClass;
        item.pkJson = RegistryDocBuilder.createPKJson(meta);
        item.dataJson = RegistryDocBuilder.createDataJson(meta, jobId);
        
        items.add(item);
    }
    
    
    public int size()
    {
        return items.size();
    }
    
    
    public boolean isEmpty()
    {
        return items.isEmpty();
    }
    
    
    public void clear()
    {
        items.clear();
    }
    
    
    public List<NJsonItem> getItems()
    {
        return items;
    }
    
    
    public List<String> getLidVids()
    {
        List<String> ids = new ArrayList<>();
        items.forEach((item) -> { ids.add(item.lidvid); } );        
        return ids;
    }
}
