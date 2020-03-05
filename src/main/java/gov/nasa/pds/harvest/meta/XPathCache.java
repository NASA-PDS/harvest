package gov.nasa.pds.harvest.meta;


import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;


public class XPathCache
{
    private static class Item
    {
        public String fieldName;
        public XPathExpression xpe;
        
        public Item(String fieldName, XPathExpression xpe)
        {
            this.fieldName = fieldName;
            this.xpe = xpe;
        }
    }

///////////////////////////////////////////////////////
    
    
    private List<Item> items;
    
    
    public XPathCache()
    {
        items = new ArrayList<>();
    }

    
    public void add(String fieldName, XPathExpression xpe)
    {
        items.add(new Item(fieldName, xpe));
    }
    
    
    public List<Item> getItems()
    {
        return items;
    }
}
