package gov.nasa.pds.harvest.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.RestClient;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.Tuple;
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
    final private static HashSet<String> alreadyLearned = new HashSet<String>();
    final private Logger log = LogManager.getLogger(RegistryDocBatch.class);
    private List<NJsonItem> items;
    
    
    /**
     * Constructor
     */
    public RegistryDocBatch()
    {
        items = new ArrayList<>();
    }

    /* hack for PDS-NASA/harvest#127
     * search the JSON string for any ref_lid_ and add if necessary to index so that it is searchable
     */
    private void updateIndex(ConnectionFactory conFact, String json) {
      int begin_index = json.indexOf("ref_lid_"), end_index;
      String name;
      if (-1 < begin_index && alreadyLearned.isEmpty()) {
        try (RestClient client = conFact.createRestClient()) {
          alreadyLearned.addAll(client.performRequest(client.createMappingRequest().setIndex(conFact.getIndexName())).fieldNames());
        } catch (Exception e) {
          log.error("Unexpected error (should not have made it here) while getting index " + conFact.getIndexName(),e);
        }
      }
      while (-1 < begin_index) {
        end_index = json.indexOf('"', begin_index+5);
        name = json.substring(begin_index, end_index);
        if (!alreadyLearned.contains(name)) {
          try (RestClient client = conFact.createRestClient()) {
            ArrayList<Tuple> new_item = new ArrayList<Tuple>();
            new_item.add(new Tuple(name, "keyword"));
            client.performRequest(client.createMappingRequest().setIndex(conFact.getIndexName()).buildUpdateFieldSchema(new_item));
            alreadyLearned.add(name);
          } catch (Exception e) {
            log.error("Unexpected error (should not have made it here) while updating index with " + name,e);
          }
        }
        begin_index = json.indexOf("ref_lid_", end_index);
      }
    }
    public void write(ConnectionFactory conFact, Metadata meta, String jobId) throws Exception
    {
        NJsonItem item = new NJsonItem();
        item.lidvid = meta.lidvid;
        item.prodClass = meta.prodClass;
        item.pkJson = RegistryDocBuilder.createPKJson(meta);
        item.dataJson = RegistryDocBuilder.createDataJson(meta, jobId);
        this.updateIndex(conFact, item.dataJson);
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
