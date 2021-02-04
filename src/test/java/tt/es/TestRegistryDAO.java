package tt.es;

import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.harvest.cfg.model.RegistryCfg;
import gov.nasa.pds.harvest.dao.RegistryDAO;
import gov.nasa.pds.harvest.dao.RegistryManager;


public class TestRegistryDAO
{

    public static void main(String[] args) throws Exception
    {
        RegistryCfg cfg = new RegistryCfg();
        cfg.url = "http://localhost:9200";
        
        RegistryManager.init(cfg);
        
        try
        {
            RegistryDAO dao = RegistryManager.getInstance().getRegistryDAO();
            
            Set<String> ids = new TreeSet<>();
            ids.add("urn:nasa:pds:orex.spice:document::6.0");
            ids.add("test1234");
    
            Set<String> retIds = dao.searchIds(ids, 100);
            for(String retId: retIds)
            {
                System.out.println(retId);
            }
        }
        finally
        {
            RegistryManager.destroy();
        }
    }

}
