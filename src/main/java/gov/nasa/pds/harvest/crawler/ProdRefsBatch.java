package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.List;


public class ProdRefsBatch
{
    public int batchNum;
    
    public List<String> lidvids = new ArrayList<>();
    public List<String> lids = new ArrayList<>();

    
    public ProdRefsBatch()
    {
    }

    
    public void clear()
    {
        lidvids.clear();
        lids.clear();
    }

    
    public void addLidVid(String value)
    {
        lidvids.add(value);
    }


    public void addLid(String value)
    {
        lids.add(value);
    }

}
