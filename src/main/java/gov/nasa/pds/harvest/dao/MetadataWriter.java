package gov.nasa.pds.harvest.dao;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.crawler.Counter;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.es.dao.DataLoader;
import gov.nasa.pds.registry.common.meta.Metadata;


public class MetadataWriter implements Closeable
{
    private final static String WARN_SKIP_PRE = "Skipping registered product ";
    private final static String WARN_SKIP_POST = " (LIDVID/LID already exists in registry database)";
    private final static int ES_DOC_BATCH_SIZE = 50;
    private final ConnectionFactory conFact;
    private Logger log;
    
    private RegistryDao registryDao;
    private DataLoader loader;
    private RegistryDocBatch docBatch;
    private String jobId;
    
    private int totalRecords;
    private boolean overwriteExisting = false;

    private Counter counter;
    
    /**
     * Constructor
     * @param cfg registry configuration
     * @throws Exception an exception
     */
    public MetadataWriter(ConnectionFactory conFact, RegistryDao dao, Counter counter) throws Exception
    {
      this.conFact = conFact;
        log = LogManager.getLogger(this.getClass());
        loader = new DataLoader(conFact);
        docBatch = new RegistryDocBatch();
        jobId = PackageIdGenerator.getInstance().getPackageId();
        
        this.registryDao = dao;
        this.counter = counter;
    }

    
    public void setOverwriteExisting(boolean b)
    {
        this.overwriteExisting = b;
    }
    
    
    public void write(Metadata meta) throws Exception
    {
        docBatch.write(this.conFact, meta, jobId);
        
        if(docBatch.size() % ES_DOC_BATCH_SIZE == 0)
        {
            writeBatch();
        }
    }

    
    private void writeBatch() throws Exception
    {
        if(docBatch.isEmpty()) return;
        
        Set<String> nonRegisteredIds = null;
        if(!overwriteExisting)
        {
            List<String> batchLidVids = docBatch.getLidVids();
            nonRegisteredIds = registryDao.getNonExistingIds(batchLidVids);
            if(nonRegisteredIds == null || nonRegisteredIds.isEmpty())
            {
                for(String lidvid: batchLidVids)
                {
                    log.warn(WARN_SKIP_PRE + lidvid + WARN_SKIP_POST);
                    counter.skippedFileCount++;
                }
                
                docBatch.clear();
                return;
            }
        }

        // Build JSON documents for Elasticsearch
        List<String> data = new ArrayList<>();
        
        for(RegistryDocBatch.NJsonItem item: docBatch.getItems())
        {
            if(nonRegisteredIds == null)
            {
                addItem(data, item);
            }
            else
            {
                if(nonRegisteredIds.contains(item.lidvid))
                {
                    addItem(data, item);
                }
                else
                {
                    log.warn(WARN_SKIP_PRE + item.lidvid + WARN_SKIP_POST);
                    counter.skippedFileCount++;
                }
            }
        }
        
        // Load batch
        Set<String> failedIds = new TreeSet<>();
        totalRecords += loader.loadBatch(data, failedIds);
        log.info("Wrote " + totalRecords + " product(s)");
        
        // Update failed counter
        counter.failedFileCount += failedIds.size();

        // Update product counters
        for(RegistryDocBatch.NJsonItem item: docBatch.getItems())
        {
            if((nonRegisteredIds == null && !failedIds.contains(item.lidvid)) ||
               (nonRegisteredIds != null && nonRegisteredIds.contains(item.lidvid) && !failedIds.contains(item.lidvid)))
            {
                counter.prodCounters.inc(item.prodClass);
            }
        }
        
        // Clear batch
        docBatch.clear();
    }

    
    private void addItem(List<String> data, RegistryDocBatch.NJsonItem item)
    {
        data.add(item.pkJson);
        data.add(item.dataJson);
    }
    
    
    public void flush() throws Exception
    {
        writeBatch();
    }
    
    
    @Override
    public void close() throws IOException
    {
        try
        {
            flush();
        }
        catch(IOException ex)
        {
            throw ex;
        }
        catch(Exception ex)
        {
            throw new IOException(ex);
        }
    }
}
