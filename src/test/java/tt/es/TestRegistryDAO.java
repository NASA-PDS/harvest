package tt.es;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.registry.common.cfg.RegistryCfg;


public class TestRegistryDAO
{
    private static String existingId = "urn:nasa:pds:orex.spice:document::6.0";
    private static String nonExistingId = "test1234";

    
    public static void main(String[] args) throws Exception
    {
        RegistryCfg cfg = new RegistryCfg();
        cfg.url = "http://localhost:9200";
        
        RegistryManager.init(cfg);

        try
        {
            RegistryDao dao = RegistryManager.getInstance().getRegistryDao();
            testIdExists(dao);
            testGetNonExistingIds(dao);
        }
        finally
        {
            RegistryManager.destroy();
        }
    }
    
    
    public static void testIdExists(RegistryDao dao) throws Exception
    {
        System.out.println("IdExists (true): passed: " + dao.idExists(existingId));
        System.out.println("IdExists (false): passed: " + (!dao.idExists(nonExistingId)));
    }

    
    public static void testGetNonExistingIds(RegistryDao dao) throws Exception
    {
        Set<String> ids = new TreeSet<>();
        ids.add(existingId);
        ids.add(nonExistingId);

        Collection<String> retIds = dao.getNonExistingIds(ids);
        
        boolean passed = (retIds.size() == 1) 
                && (retIds.iterator().next().equals(nonExistingId));
        
        System.out.println("getNonExistingIds: passed: " + passed);
    }
}
