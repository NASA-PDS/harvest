package gov.nasa.pds.harvest.meta;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XPathCache;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.registry.common.util.date.PdsDateConverter;


/**
 * Extract data from PDS4 label by XPath
 * @author karpenko
 */
public class XPathExtractor
{
    private PdsDateConverter dateConverter;
    
    /**
     * Constructor
     */
    public XPathExtractor()
    {
        dateConverter = new PdsDateConverter(true);
    }

    
    public void extract(Document doc, FieldMap fields) throws Exception
    {
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Common fields
        XPathCache cache = XPathCacheManager.getInstance().getCommonCache();
        addFields(doc, cache, fields);
        
        // Object type fields
        cache = XPathCacheManager.getInstance().getCacheByObjectType(rootElement);
        addFields(doc, cache, fields);
    }
    
    
    private void addFields(Document doc, XPathCache cache, FieldMap fieldMap) throws Exception
    {
        if(cache == null || cache.isEmpty()) return;
        
        for(XPathCache.Item item: cache.getItems())
        {
            String[] values = XPathUtils.getStringArray(doc, item.xpe);
            
            if(item.dataType == XPathCache.Item.TYPE_DATE)
            {
                addDates(item.fieldName, values, fieldMap);
            }
            else
            {
                fieldMap.addValues(item.fieldName, values);
            }
        }
    }

    
    private void addDates(String fieldName, String[] values, FieldMap fieldMap) throws Exception
    {
        if(values == null || values.length == 0) return;
        
        String[] newValues = new String[values.length];
        
        for(int i = 0; i < values.length; i++)
        {
            newValues[i] = dateConverter.toIsoInstantString(fieldName, values[i]);
        }
        
        fieldMap.addValues(fieldName, newValues);
    }
}
