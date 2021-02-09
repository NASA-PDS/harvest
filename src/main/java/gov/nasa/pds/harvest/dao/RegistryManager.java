package gov.nasa.pds.harvest.dao;

import org.elasticsearch.client.RestClient;

import gov.nasa.pds.harvest.cfg.model.RegistryCfg;
import gov.nasa.pds.harvest.util.CloseUtils;
import gov.nasa.pds.registry.common.es.client.EsClientFactory;


public class RegistryManager
{
    private static RegistryManager instance = null;
    
    private RestClient esClient;
    private RegistryDAO registryDAO;
    
    
    private RegistryManager(RegistryCfg cfg) throws Exception
    {
        if(cfg.url == null || cfg.url.isEmpty()) throw new IllegalArgumentException("Missing Registry URL");
        
        esClient = EsClientFactory.createRestClient(cfg.url, cfg.authFile);
        
        String indexName = cfg.indexName;
        if(indexName == null || indexName.isEmpty()) indexName = "registry";
        
        registryDAO = new RegistryDAO(esClient, indexName);
    }
    
    
    public static void init(RegistryCfg cfg) throws Exception
    {
        // Registry is not configured. Run Harvest without Registry.
        if(cfg == null) return;
        
        instance = new RegistryManager(cfg);
    }
    
    
    public static void destroy()
    {
        if(instance == null) return;
        
        CloseUtils.close(instance.esClient);
        instance = null;
    }
    
    
    public static RegistryManager getInstance()
    {
        return instance;
    }
    
    
    public RegistryDAO getRegistryDAO()
    {
        return registryDAO;
    }
}
