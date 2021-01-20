package gov.nasa.pds.harvest.util.out;

import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;


public abstract class BaseRefsDocWriter implements RefsDocWriter
{
    protected Logger log;
    protected Writer writer;
    
    protected List<String> lidvidList = new ArrayList<>();
    protected List<String> lidList = new ArrayList<>();
    protected int BATCH_SIZE = 1000;

    
    public BaseRefsDocWriter()
    {
    }
    
    
    protected int getNextBatch(BufferedReader rd) throws Exception
    {
        lidvidList.clear();
        lidList.clear();
        
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
                    lidvidList.add(ref);
                    lidList.add(ref.substring(0, idx));
                }
                // lid reference
                else
                {
                    lidList.add(ref);
                }
                
                if(count >= BATCH_SIZE) return count;
            }
        }

        return count;
    }

}
