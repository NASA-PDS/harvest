package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.List;


public class ProdRefsBatch
{
    public int batchNum;
    
    public List<String> lidvids = new ArrayList<>();
    public List<String> lids = new ArrayList<>();

    public List<String> secLidvids = new ArrayList<>();
    public List<String> secLids = new ArrayList<>();

    
    public ProdRefsBatch()
    {
    }

    
    public void clear()
    {
        lidvids.clear();
        lids.clear();

        secLidvids.clear();
        secLids.clear();
    }

    
    public void addLidVid(String value, boolean isPrimary)
    {
        if(isPrimary)
        {
            lidvids.add(value);
        }
        else
        {
            secLidvids.add(value);
        }
    }


    public void addLid(String value, boolean isPrimary)
    {
        if(isPrimary)
        {
            lids.add(value);
        }
        else
        {
            secLids.add(value);
        }
    }

}
