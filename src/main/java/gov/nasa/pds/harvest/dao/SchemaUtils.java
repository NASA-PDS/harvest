package gov.nasa.pds.harvest.dao;

import java.util.Set;

import gov.nasa.pds.registry.common.meta.FieldNameCache;


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
        Set<String> fields = schemaDao.getFieldNames();
        FieldNameCache.getInstance().set(fields);
    }
}
