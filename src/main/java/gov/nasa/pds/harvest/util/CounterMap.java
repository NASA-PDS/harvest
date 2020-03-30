package gov.nasa.pds.harvest.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


public class CounterMap
{
    public static class Item
    {
        public String name;
        public int count;
        
        public Item(String name)
        {
            this.name = name;
        }
        
        public void inc()
        {
            count++;
        }
    }
    
    private Map<String, Item> map;
    private int total;
    
    
    public CounterMap()
    {
        map = new TreeMap<>();
    }
    
    
    public void inc(String name)
    {
        Item item = getOrCreate(name);
        item.inc();
        
        total++;
    }
    
    
    public Collection<Item> getCounts()
    {
        return map.values();
    }
    
    
    public int getTotal()
    {
        return total;
    }
    
    
    private Item getOrCreate(String name)
    {
        Item item = map.get(name);
        if(item == null)
        {
            item = new Item(name);
            map.put(name, item);
        }
        
        return item;
    }
}
