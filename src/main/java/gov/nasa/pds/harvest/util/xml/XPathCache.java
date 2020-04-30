package gov.nasa.pds.harvest.util.xml;


import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;


public class XPathCache
{
    public static class Item
    {
        public static final int TYPE_STRING = 0;
        public static final int TYPE_DATE = 1;
        
        public String fieldName;
        public int dataType;
        public XPathExpression xpe;
        
        public Item(String fieldName, XPathExpression xpe)
        {
            this.dataType = TYPE_STRING;
            this.fieldName = fieldName;
            this.xpe = xpe;
        }
        
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
    
    
    public XPathCache()
    {
        items = new ArrayList<>();
    }

    
    public boolean isEmpty()
    {
        if(items == null || items.size() == 0) return true;
        
        return false;
    }
    
    
    public void add(String fieldName, String dataType, XPathExpression xpe)
    {
        Item item = new Item(fieldName, xpe);
        item.setDataType(dataType);
        items.add(item);
    }
    
    
    public List<Item> getItems()
    {
        return items;
    }
}
