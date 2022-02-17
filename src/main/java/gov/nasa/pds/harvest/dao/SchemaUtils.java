package gov.nasa.pds.harvest.dao;

import gov.nasa.pds.registry.common.meta.FieldNameCache;
import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.es.dao.schema.SchemaDao;


/**
 * Utility methods to work with elasticsearch schema
 * @author karpenko
 */
public class SchemaUtils
{
    /**
     * Update fields cache of Elasticsearch registry index.
     * @throws Exception
     */
    public static void updateFieldsCache() throws Exception
    {
        SchemaDao schemaDao = RegistryManager.getInstance().getSchemaDao();
        DataDictionaryDao ddDao = RegistryManager.getInstance().getDataDictionaryDao();
        
        FieldNameCache cache = FieldNameCache.getInstance();
        
        cache.setSchemaFieldNames(schemaDao.getFieldNames());
        cache.setBooleanFieldNames(ddDao.getFieldNamesByEsType("boolean"));
        cache.setDateFieldNames(ddDao.getFieldNamesByEsType("date"));
    }
}
