package gov.nasa.pds.harvest.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.logging.log4j.Logger;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.out.RefsBatch;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;


public class CollectionInventoryProcessor
{
    protected Logger log;
    
    private int BATCH_SIZE = 1000;
    private RefsBatch batch = new RefsBatch();
    
    private RefsDocWriter writer;
    
    
    public CollectionInventoryProcessor(RefsDocWriter writer)
    {
        this.writer = writer;
    }
    
    
    public void writeCollectionInventory(Metadata meta, File inventoryFile) throws Exception
    {
        BufferedReader rd = new BufferedReader(new FileReader(inventoryFile));
        
        batch.batchNum = 0;
        
        while(true)
        {
            int count = getNextBatch(rd);
            if(count == 0) break;
            
            batch.batchNum++;            
            writer.writeBatch(meta, batch);
            
            if(count < BATCH_SIZE) break;
        }
        
        rd.close();
    }
    
    
    protected int getNextBatch(BufferedReader rd) throws Exception
    {
        batch.lidvidList.clear();
        batch.lidList.clear();
        
        String line;
        int count = 0;
        
        while((line = rd.readLine()) != null)
        {
            if(line.isBlank()) continue;
            String[] tokens = line.split(",");
            if(tokens.length != 2)
            {
                log.warn("Invalid collection inventory record: " + line);
                continue;
            }
            
            if("P".equalsIgnoreCase(tokens[0].trim()))
            {
                count++;
                String ref = tokens[1].trim();
                int idx = ref.indexOf("::");
                
                // This is a lidvid reference
                if(idx > 0)
                {
                    batch.lidvidList.add(ref);
                }
                // lid reference
                else
                {
                    batch.lidList.add(ref);
                }
                
                if(count >= BATCH_SIZE) return count;
            }
        }

        return count;
    }

}
