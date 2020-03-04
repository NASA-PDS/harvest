package gov.nasa.pds.harvest.meta;


import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;


public class XPathCache
{
    private static class Item
    {
        public XPathExpression xpe;
        public String fieldName;
    }

///////////////////////////////////////////////////////
    
    
    private List<Item> items;
    
    
    public XPathCache()
    {
        items = new ArrayList<>();
    }

    
    public void add(String fieldName, String xpath)
    {
        
    }
}
