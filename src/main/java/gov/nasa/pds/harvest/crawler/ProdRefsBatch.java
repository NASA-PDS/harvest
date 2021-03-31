package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.List;


public class ProdRefsBatch
{
    public int batchNum;
    public int size;
    
    public List<String> lidvids = new ArrayList<>();
    public List<String> lids = new ArrayList<>();

    
    public ProdRefsBatch()
    {
    }

    
    public void clear()
    {
        lidvids.clear();
        lids.clear();
        size = 0;
    }

    
    public void addLidVid(String value)
    {
        lidvids.add(value);
        size++;
    }


    public void addLid(String value)
    {
        lids.add(value);
        size++;
    }

}
