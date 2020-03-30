package gov.nasa.pds.harvest.crawler;

import gov.nasa.pds.harvest.util.CounterMap;

public class Counter
{
    public CounterMap prodCounters;
    public int skippedFileCount;
    
    public Counter()
    {
        prodCounters = new CounterMap();
    }
}
