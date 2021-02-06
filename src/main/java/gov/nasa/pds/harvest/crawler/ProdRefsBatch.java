package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public interface ProdRefsBatch
{
    public void clear();
    
    public void incBatchNum();    
    public int getBatchNum();
    
    public void addLidVid(String value);
    public void addLid(String value);
    
    
    public static class WriterBatch implements ProdRefsBatch
    {
        public int batchNum;
        public List<String> lidvids = new ArrayList<>();
        public List<String> lids = new ArrayList<>();

        
        public WriterBatch()
        {
        }

        
        @Override
        public void clear()
        {
            lidvids.clear();
            lids.clear();
        }

        @Override
        public void incBatchNum()
        {
            batchNum++;
        }

        @Override
        public int getBatchNum()
        {
            return batchNum;
        }

        @Override
        public void addLidVid(String value)
        {
            lidvids.add(value);
        }

        @Override
        public void addLid(String value)
        {
            lids.add(value);
        }
    }


    public static class ElasticSearchBatch implements ProdRefsBatch
    {
        public Set<String> lidvids = new TreeSet<>();
        public List<String> lids = new ArrayList<>();

        
        public ElasticSearchBatch()
        {
        }

        
        @Override
        public void clear()
        {
            lidvids.clear();
            lids.clear();
        }

        @Override
        public void incBatchNum()
        {
        }

        @Override
        public int getBatchNum()
        {
            return 0;
        }

        @Override
        public void addLidVid(String value)
        {
            lidvids.add(value);
        }

        @Override
        public void addLid(String value)
        {
            lids.add(value);
        }
        
    }

}
