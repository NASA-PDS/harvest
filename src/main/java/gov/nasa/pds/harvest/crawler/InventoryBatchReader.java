package gov.nasa.pds.harvest.crawler;

import java.io.BufferedReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.util.CloseUtils;


/**
 * A class to read records from collection inventory files in batches.
 *  
 * @author karpenko
 */
public class InventoryBatchReader
{
    private Logger log;
    private BufferedReader rd;
    private RefType readRefType;

    /**
     * Constructor
     * @param reader Usually a file reader.
     * @param refType Primary or secondary product reference
     */
    public InventoryBatchReader(Reader reader, RefType refType)
    {
        log = LogManager.getLogger(this.getClass());
        rd = new BufferedReader(reader);
        this.readRefType = refType;
    }
    
    
    /**
     * Close the reader (input file).
     */
    public void close()
    {
        CloseUtils.close(rd);
    }
    
    
    /**
     * Read next batch from a collection inventory file.
     * @param batchSize Batch size
     * @param batch Store lid and lidvid references in this object (This is the output parameter)
     * @return number of records read.
     * @throws Exception Generic exception
     */
    public int readNextBatch(int batchSize, ProdRefsBatch batch) throws Exception
    {
        batch.clear();
        batch.batchNum++;
        
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
                        
            RefType refType = "P".equalsIgnoreCase(tokens[0].trim()) ? RefType.PRIMARY : RefType.SECONDARY; 
            if(readRefType != refType) continue;

            count++;

            String ref = tokens[1].trim();
            int idx = ref.indexOf("::");
            
            // This is a lidvid reference
            if(idx > 0)
            {
                batch.addLidVid(ref);
            }
            // lid reference
            else
            {
                batch.addLid(ref);
            }
            
            if(count >= batchSize) return count;
        }

        return count;
    }

}
