package gov.nasa.pds.harvest.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;

import gov.nasa.pds.harvest.crawler.Counter;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.registry.common.cfg.RegistryCfg;
import gov.nasa.pds.registry.common.es.client.EsClientFactory;
import gov.nasa.pds.registry.common.util.CloseUtils;
import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.es.dao.schema.SchemaDao;
import gov.nasa.pds.registry.common.es.service.MissingFieldsProcessor;
import gov.nasa.pds.registry.common.es.service.SchemaUpdater;
import gov.nasa.pds.registry.common.meta.FieldNameCache;
import gov.nasa.pds.registry.common.meta.MetadataNormalizer;


/**
 * A singleton object to query Elasticsearch.
 *  
 * @author karpenko
 */
public class RegistryManager
{
    private static RegistryManager instance = null;
    
    private RegistryCfg cfg;
    
    private RestClient esClient;
    private RegistryDao registryDao;
    private SchemaDao schemaDao;
    private DataDictionaryDao ddDao;

    private RegistryWriter registryWriter;
    private FieldNameCache fieldNameCache;

    private Counter counter;
    
    /**
     * Private constructor. Use getInstance() instead.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @throws Exception Generic exception
     */
    private RegistryManager(RegistryCfg cfg) throws Exception
    {
        this.cfg = cfg;
        this.counter = new Counter();
        
        Logger log = LogManager.getLogger(this.getClass());
        log.log(LogUtils.LEVEL_SUMMARY, "Elasticsearch URL: " + cfg.url + ", index: " + cfg.indexName);
        
        esClient = EsClientFactory.createRestClient(cfg.url, cfg.authFile);
        
        String indexName = cfg.indexName;
        if(indexName == null || indexName.isEmpty()) indexName = "registry";
        
        registryDao = new RegistryDao(esClient, indexName);
        schemaDao = new SchemaDao(esClient, indexName);
        ddDao = new DataDictionaryDao(esClient, indexName);
        
        fieldNameCache = new FieldNameCache(ddDao, schemaDao);
        registryWriter = new RegistryWriter(cfg, registryDao, counter);
    }
    
    
    /**
     * Initialize the singleton.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @throws Exception Generic exception
     */
    public static void init(RegistryCfg cfg) throws Exception
    {
        // Registry is not configured. Run Harvest without Registry.
        if(cfg == null) throw new IllegalArgumentException("Registry is not configuraed.");
        if(cfg.url == null || cfg.url.isEmpty()) throw new IllegalArgumentException("Missing Registry URL");        
        
        instance = new RegistryManager(cfg);
    }
    
    
    /**
     * Clean up resources (close Elasticsearch client / connection).
     */
    public static void destroy()
    {
        if(instance == null) return;
        
        CloseUtils.close(instance.registryWriter);
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
     * Get registry configuration.
     * @return Registry configuration
     */
    public RegistryCfg getRegistryConfiguration()
    {
        return cfg;
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

    
    /**
     * Get Data Dictionary DAO object.
     * @return Schema DAO
     */
    public DataDictionaryDao getDataDictionaryDao()
    {
        return ddDao;
    }

    
    /**
     * Get Field name cache
     * @return Schema DAO
     */
    public FieldNameCache getFieldNameCache()
    {
        return fieldNameCache;
    }

    
    /**
     * Create new missing field processor
     * @return new missing field processor object
     * @throws Exception an exception
     */
    public MissingFieldsProcessor createMissingFieldsProcessor() throws Exception
    {
        SchemaUpdater su = new SchemaUpdater(cfg, ddDao, schemaDao);
        return new MissingFieldsProcessor(su, fieldNameCache);
    }


    /**
     * Create new metadata normalizer
     * @return new metadata normalizer object
     */
    public MetadataNormalizer createMetadataNormalizer()
    {
        return new MetadataNormalizer(fieldNameCache);
    }
    
    
    /**
     * Get registry writer
     * @return registry writer
     */
    public RegistryWriter getRegistryWriter()
    {
        return registryWriter;
    }
    
    
    /**
     * Get counter of processed products
     * @return counter
     */
    public Counter getCounter()
    {
        return counter;
    }
}
