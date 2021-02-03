package tt.es;

import java.util.Set;
import java.util.TreeSet;

import org.elasticsearch.client.RestClient;

import gov.nasa.pds.harvest.dao.RegistryDAO;
import gov.nasa.pds.registry.common.es.client.EsClientFactory;


public class TestRegistryDAO
{

    public static void main(String[] args) throws Exception
    {
        RestClient esClient = EsClientFactory.createRestClient("http://localhost:9200", null);
        RegistryDAO dao = new RegistryDAO(esClient, "registry", true);
        
        Set<String> ids = new TreeSet<>();
        ids.add("urn:nasa:pds:orex.spice:document::6.0");
        ids.add("test1234");

        dao.removeExistingIds(ids, 100);
    }

}
