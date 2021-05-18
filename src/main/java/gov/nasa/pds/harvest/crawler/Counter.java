package gov.nasa.pds.harvest.crawler;

import gov.nasa.pds.harvest.util.CounterMap;

/**
 * Counter of processed products.
 * 
 * @author karpenko
 */
public class Counter
{
    public CounterMap prodCounters;
    public int skippedFileCount;
    
    public Counter()
    {
        prodCounters = new CounterMap();
    }
}
