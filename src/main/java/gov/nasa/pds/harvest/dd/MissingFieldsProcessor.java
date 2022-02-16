package gov.nasa.pds.harvest.dd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.pds.registry.common.meta.FieldNameCache;
import gov.nasa.pds.registry.common.meta.MetaConstants;
import gov.nasa.pds.registry.common.util.FieldMap;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;

public class MissingFieldsProcessor
{
    protected Set<String> missingFields;
    protected Map<String, String> missingXsds;

    
    public MissingFieldsProcessor()
    {
        missingFields = new HashSet<>();
        missingXsds = new HashMap<>();
    }

    
    public void processDoc(FieldMap fmap, XmlNamespaces xmlns)
    {
        for(String key: fmap.getNames())
        {
            // Check if current Elasticsearch schema has this field.
            if(!FieldNameCache.getInstance().containsName(key))
            {
                // Update missing fields and XSDs
                missingFields.add(key);
                updateMissingXsds(key, xmlns);
            }
        }
        
        System.out.println("***************" + missingFields.size() + ", " + missingXsds.size());
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
