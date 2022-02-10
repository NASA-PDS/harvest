package gov.nasa.pds.harvest.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;

import gov.nasa.pds.harvest.cfg.model.RegistryCfg;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.registry.common.es.client.EsClientFactory;
import gov.nasa.pds.registry.common.util.CloseUtils;


/**
 * A singleton object to query Elasticsearch.
 *  
 * @author karpenko
 */
public class RegistryManager
{
    private static RegistryManager instance = null;
    
    private RestClient esClient;
    private RegistryDao registryDao;
    private SchemaDao schemaDao;
    
    
    /**
     * Private constructor. Use getInstance() instead.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @throws Exception Generic exception
     */
    private RegistryManager(RegistryCfg cfg) throws Exception
    {
        if(cfg.url == null || cfg.url.isEmpty()) throw new IllegalArgumentException("Missing Registry URL");
        
        Logger log = LogManager.getLogger(this.getClass());
        log.log(LogUtils.LEVEL_SUMMARY, "Elasticsearch URL: " + cfg.url + ", index: " + cfg.indexName);
        
        esClient = EsClientFactory.createRestClient(cfg.url, cfg.authFile);
        
        String indexName = cfg.indexName;
        if(indexName == null || indexName.isEmpty()) indexName = "registry";
        
        registryDao = new RegistryDao(esClient, indexName);
        schemaDao = new SchemaDao(esClient, indexName);
    }
    
    
    /**
     * Initialize the singleton.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @throws Exception Generic exception
     */
    public static void init(RegistryCfg cfg) throws Exception
    {
        // Registry is not configured. Run Harvest without Registry.
        if(cfg == null) throw new Exception("Registry is not configuraed.");
        
        instance = new RegistryManager(cfg);
    }
    
    
    /**
     * Clean up resources (close Elasticsearch client / connection).
     */
    public static void destroy()
    {
        if(instance == null) return;
        
        CloseUtils.close(instance.esClient);
        instance = null;
    }
    
    
    /**
     * Get the singleton instance.
     * @return Registry manager singleton
     */
    public static RegistryManager getInstance()
    {
        return instance;
    }
    
    
    /**
     * Get registry DAO object.
     * @return Registry DAO
     */
    public RegistryDao getRegistryDao()
    {
        return registryDao;
    }


    /**
     * Get schema DAO object.
     * @return Schema DAO
     */
    public SchemaDao getSchemaDao()
    {
        return schemaDao;
    }

}
