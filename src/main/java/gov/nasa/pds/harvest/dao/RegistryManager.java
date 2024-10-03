package gov.nasa.pds.harvest.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.crawler.Counter;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.RestClient;
import gov.nasa.pds.registry.common.util.CloseUtils;
import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.es.dao.schema.SchemaDao;
import gov.nasa.pds.registry.common.es.service.CollectionInventoryWriter;
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
    
    private ConnectionFactory conFact;
    private boolean overwriteFlag;
    
    private RestClient client;

    final private RegistryDao registryDao;
    final private SchemaDao schemaDao;
    final private DataDictionaryDao ddDao;

    private MetadataWriter registryWriter;
    private CollectionInventoryWriter invWriter;

    final private FieldNameCache fieldNameCache;

    private Counter counter;
    
    
    /**
     * Private constructor. Use getInstance() instead.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @throws Exception Generic exception
     */
    private RegistryManager(ConnectionFactory conFact, boolean overwriteFlag) throws Exception
    {
        this.conFact = conFact;
        this.overwriteFlag = overwriteFlag;
        this.counter = new Counter();
        Logger log = LogManager.getLogger(this.getClass());
        log.log(LogUtils.LEVEL_SUMMARY, "Connection: " + conFact);
        client = conFact.createRestClient();
        registryDao = new RegistryDao(client, conFact.getIndexName());
        schemaDao = new SchemaDao(client, conFact.getIndexName());
        ddDao = new DataDictionaryDao(client, conFact.getIndexName());
        fieldNameCache = new FieldNameCache(ddDao, schemaDao);
        registryWriter = new MetadataWriter(conFact, registryDao, counter);
        registryWriter.setOverwriteExisting(overwriteFlag);
        invWriter = new CollectionInventoryWriter(conFact);
        
        if (!this.client.exists(this.conFact.getIndexName())) {
          throw new RuntimeException("The index '" + this.conFact.getIndexName() + "' does not exist. Please create it first.");
        }
    }
    
    
    /**
     * Initialize the singleton.
     * @param cfg Registry (Elasticsearch) configuration parameters.
     * @param overwriteFlag overwrite registered products
     * @throws Exception Generic exception
     */
    public static synchronized void init(ConnectionFactory conFact, boolean overwriteFlag) throws Exception
    {
      if (instance == null) {
        instance = new RegistryManager(conFact, overwriteFlag);
      }
    }
    
    
    /**
     * Clean up resources (close Elasticsearch client / connection).
     */
    public static void destroy()
    {
        if(instance == null) return;
        
        CloseUtils.close(instance.registryWriter);
        CloseUtils.close(instance.client);
        
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
     * Get overwrite flag
     * @return if true, overwrite already registered documents
     */
    public boolean isOverwrite()
    {
        return overwriteFlag;
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
        SchemaUpdater su = new SchemaUpdater(conFact, ddDao, schemaDao);
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
    public MetadataWriter getRegistryWriter()
    {
        return registryWriter;
    }
    
    
    /**
     * Get collection inventory writer
     * @return collection inventory writer
     */
    public CollectionInventoryWriter getCollectionInventoryWriter()
    {
        return invWriter;
    }
    
    /**
     * Get counter of processed products
     * @return counter
     */
    public Counter getCounter()
    {
        return counter;
    }
    public String getIndexName() {
      return this.conFact.getIndexName();
    }
}
