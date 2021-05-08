package gov.nasa.pds.harvest.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


/**
 * Product counters (by product type).
 * 
 * @author karpenko
 */
public class CounterMap
{
    /**
     * Inner class to store counters by key.
     * @author karpenko
     */
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
    

    /**
     * Constructor
     */
    public CounterMap()
    {
        map = new TreeMap<>();
    }
    
    
    /**
     * Increment counter for the given key (product type).
     * @param name
     */
    public void inc(String name)
    {
        Item item = getOrCreate(name);
        item.inc();
        
        total++;
    }
    
    
    /**
     * Get all counters grouped by key.
     * @return
     */
    public Collection<Item> getCounts()
    {
        return map.values();
    }
    
    
    /**
     * Get total count.
     * @return
     */
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
