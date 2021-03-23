package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.dao.RegistryDAO;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;


public class CollectionInventoryProcessor
{
    protected Logger log;
    
    private int WRITE_BATCH_SIZE = 500;
    private int ELASTIC_BATCH_SIZE = 50;
    
    private ProdRefsBatch batch = new ProdRefsBatch();
    private RefsDocWriter writer;
    private boolean primaryOnly;
    
    
    public CollectionInventoryProcessor(RefsDocWriter writer, boolean primaryOnly)
    {
        log = LogManager.getLogger(this.getClass());
        this.writer = writer;
        this.primaryOnly = primaryOnly;
    }
    
    
    public void writeCollectionInventory(Metadata meta, File inventoryFile, boolean cacheProductIds) throws Exception
    {
        batch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        
        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), primaryOnly);
        
        while(true)
        {
            int count = rd.readNextBatch(WRITE_BATCH_SIZE, batch);
            if(count == 0) break;
            
            // Update cache. Only products in cache will be processed.
            if(cacheProductIds)
            {
                cache.addLidVids(batch.lidvids);
                cache.addLids(batch.lids);
            }
            
            // Write batch
            writer.writeBatch(meta, batch);
            
            if(count < WRITE_BATCH_SIZE) break;
        }
        
        rd.close();
    }
    
    
    public void cacheNonRegisteredInventory(Metadata meta, File inventoryFile) throws Exception
    {
        if(RegistryManager.getInstance() == null) throw new Exception("Registry is not configured");

        batch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        RegistryDAO dao = RegistryManager.getInstance().getRegistryDAO(); 

        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), primaryOnly);
        
        while(true)
        {
            int count = rd.readNextBatch(ELASTIC_BATCH_SIZE, batch);
            if(count == 0) break;

            Collection<String> nonRegisteredIds = dao.getNonExistingIds(batch.lidvids, ELASTIC_BATCH_SIZE);
            
            // Update cache. Only products in cache will be processed.
            cache.addLidVids(nonRegisteredIds);
            cache.addLids(batch.lids);
            
            if(count < ELASTIC_BATCH_SIZE) break;
        }
        
        rd.close();        
    }
    

}
