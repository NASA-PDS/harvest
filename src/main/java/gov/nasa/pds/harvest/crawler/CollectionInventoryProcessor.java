package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.io.FileReader;
import java.util.List;

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
    
    private ProdRefsBatch.WriterBatch writerBatch = new ProdRefsBatch.WriterBatch();
    private ProdRefsBatch.ElasticSearchBatch esBatch = new ProdRefsBatch.ElasticSearchBatch();
    
    private RefsDocWriter writer;
    
    
    public CollectionInventoryProcessor(RefsDocWriter writer)
    {
        log = LogManager.getLogger(this.getClass());
        this.writer = writer;
    }
    
    
    public void writeCollectionInventory(Metadata meta, File inventoryFile) throws Exception
    {
        writerBatch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        
        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile));
        
        while(true)
        {
            int count = rd.readNextBatch(WRITE_BATCH_SIZE, writerBatch);
            if(count == 0) break;
            
            // Update cache. Only products in cache will be processed.
            cache.addLidVids(writerBatch.lidvids);
            cache.addLids(writerBatch.lids);
            
            // Write batch
            writer.writeBatch(meta, writerBatch);
            
            if(count < WRITE_BATCH_SIZE) break;
        }
        
        rd.close();
    }
    
    
    public void cacheNonRegisteredCollectionInventory(Metadata meta, File inventoryFile) throws Exception
    {
        if(RegistryManager.getInstance() == null) throw new Exception("Registry is not configured");

        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        RegistryDAO dao = RegistryManager.getInstance().getRegistryDAO(); 

        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile));
        
        while(true)
        {
            int count = rd.readNextBatch(ELASTIC_BATCH_SIZE, esBatch);
            if(count == 0) break;

            List<String> nonRegisteredIds = dao.getNonExistingIds(esBatch.lidvids, ELASTIC_BATCH_SIZE);
            
            // Update cache. Only products in cache will be processed.
            cache.addLidVids(nonRegisteredIds);
            cache.addLids(esBatch.lids);
            
            if(count < WRITE_BATCH_SIZE) break;
        }
        
        rd.close();        
    }
    
    

}
