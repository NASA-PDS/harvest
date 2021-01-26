package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ProdRefsBatch
{
    public int batchNum;
    private List<String> lidvids = new ArrayList<>();
    private List<String> lids = new ArrayList<>();
    
    
    public ProdRefsBatch()
    {
    }
    
    
    public void clear()
    {
        lidvids.clear();
        lids.clear();
    }
    
    
    public Collection<String> getLidVids()
    {
        return lidvids;
    }

    
    public Collection<String> getLids()
    {
        return lids;
    }

    
    public void addLid(String value)
    {
        lids.add(value);
    }


    public void addLidVid(String value)
    {
        lidvids.add(value);
    }

}
