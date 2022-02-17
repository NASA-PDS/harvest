package gov.nasa.pds.harvest.dd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.dao.SchemaUtils;
import gov.nasa.pds.registry.common.cfg.RegistryCfg;
import gov.nasa.pds.registry.common.es.dao.dd.DataDictionaryDao;
import gov.nasa.pds.registry.common.es.dao.schema.SchemaDao;
import gov.nasa.pds.registry.common.es.service.SchemaUpdater;
import gov.nasa.pds.registry.common.meta.FieldNameCache;
import gov.nasa.pds.registry.common.meta.MetaConstants;
import gov.nasa.pds.registry.common.util.FieldMap;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;

/**
 * Process fields present in PDS4 label, but missing from Elasticsearch "registry" index.
 * @author karpenko
 */
public class MissingFieldsProcessor
{
    private Set<String> missingFields;
    private Map<String, String> missingXsds;

    private SchemaUpdater sUpdater;
    
    
    /**
     * Constructor.
     * NOTE: Init registry manager before calling this constructor
     * @throws Exception and exception.
     */
    public MissingFieldsProcessor() throws Exception
    {
        missingFields = new HashSet<>();
        missingXsds = new HashMap<>();
        
        RegistryManager mgr = RegistryManager.getInstance();
        
        RegistryCfg cfg = mgr.getRegistryConfiguration();
        DataDictionaryDao ddDao = mgr.getDataDictionaryDao();
        SchemaDao sDao = mgr.getSchemaDao();
        
        sUpdater = new SchemaUpdater(cfg, ddDao, sDao);
    }

    
    public void processDoc(FieldMap fmap, XmlNamespaces xmlns) throws Exception
    {
        // Find fields not in Elasticsearch "registry" schema
        for(String key: fmap.getNames())
        {
            // Check if current Elasticsearch schema has this field.
            if(!FieldNameCache.getInstance().schemaContainsField(key))
            {
                // Update missing fields and XSDs
                missingFields.add(key);
                updateMissingXsds(key, xmlns);
            }
        }
        
        // Update LDDs and schema
        if(!missingFields.isEmpty())
        {
            try
            {
                sUpdater.updateSchema(missingFields, missingXsds);
                SchemaUtils.updateFieldsCache();
            }
            finally
            {
                missingFields.clear();
                missingXsds.clear();
            }
        }
    }
    
    
    protected void updateMissingXsds(String name, XmlNamespaces xmlns)
    {
        int idx = name.indexOf(MetaConstants.NS_SEPARATOR);
        if(idx <= 0) return;
        
        String prefix = name.substring(0, idx);
        String xsd = xmlns.prefix2location.get(prefix);
 
        if(xsd != null)
        {
            missingXsds.put(xsd, prefix);
        }
    }

        
}
