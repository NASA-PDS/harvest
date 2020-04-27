package gov.nasa.pds.harvest.meta;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XPathCache;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class XPathExtractor
{
    private XPathExtractor()
    {
    }

    
    public static void extract(Document doc, FieldMap fields) throws Exception
    {
        String rootElement = doc.getDocumentElement().getNodeName();
        
        // Common fields
        XPathCache cache = XPathCacheManager.getInstance().getCommonCache();
        addFields(doc, cache, fields);
        
        // Object type fields
        cache = XPathCacheManager.getInstance().getCacheByObjectType(rootElement);
        addFields(doc, cache, fields);
    }
    
    
    private static void addFields(Document doc, XPathCache cache, FieldMap fieldMap) throws Exception
    {
        if(cache == null || cache.isEmpty()) return;
        
        for(XPathCache.Item item: cache.getItems())
        {
            String[] values = XPathUtils.getStringArray(doc, item.xpe);
            fieldMap.addValues(item.fieldName, values);
        }
    }

}
