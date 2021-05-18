package gov.nasa.pds.harvest.util.xml;


import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;


/**
 * XPath cache is used to map a field in a PDS4 label (by its XPath) 
 * to a registry field name. 
 * This is a legacy feature which might be removed in future releases.
 * 
 * @author karpenko
 */
public class XPathCache
{
    /**
     * Inner class representing one cache record / item. 
     * @author karpenko
     */
    public static class Item
    {
        public static final int TYPE_STRING = 0;
        public static final int TYPE_DATE = 1;
        
        public String fieldName;
        public int dataType;
        public XPathExpression xpe;
        
        /**
         * Constructor
         * @param fieldName Field name
         * @param xpe an XPath
         */
        public Item(String fieldName, XPathExpression xpe)
        {
            this.dataType = TYPE_STRING;
            this.fieldName = fieldName;
            this.xpe = xpe;
        }
        
        /**
         * Set item data type.
         * @param str Data type: "date" or "string".
         */
        public void setDataType(String str)
        {
            if("date".equalsIgnoreCase(str))
            {
                dataType = TYPE_DATE;
            }
            else
            {
                dataType = TYPE_STRING;
            }
        }
    }

///////////////////////////////////////////////////////
    
    
    private List<Item> items;
    
    
    /**
     * Constructor.
     */
    public XPathCache()
    {
        items = new ArrayList<>();
    }

    
    /**
     * Check if cache is empty.
     * @return check if cache is empty
     */
    public boolean isEmpty()
    {
        if(items == null || items.size() == 0) return true;
        
        return false;
    }
    
    
    /**
     * Get an item to the cache.
     * @param fieldName Field name
     * @param dataType data type
     * @param xpe an XPath
     */
    public void add(String fieldName, String dataType, XPathExpression xpe)
    {
        Item item = new Item(fieldName, xpe);
        item.setDataType(dataType);
        items.add(item);
    }
    
    
    /**
     * Get all cached items.
     * @return All cached items
     */
    public List<Item> getItems()
    {
        return items;
    }
}
