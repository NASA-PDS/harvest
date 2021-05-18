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


/**
 * <p>Process inventory files of "Product_Collection" products (PDS4 label files)
 * 
 * <p>Parse collection inventory file, e.g., "document_collection_inventory.csv",
 * extract primary and secondary references (lidvids) and write extracted data
 * into a JSON or XML file. JSON files can be imported into Elasticsearch by 
 * Registry Manager tool.
 * 
 * <p>This class also uses "RefsCache" singleton to cache product ids (lidvids).
 * 
 * @author karpenko
 */
public class CollectionInventoryProcessor
{
    protected Logger log;
    
    private int WRITE_BATCH_SIZE = 500;
    private int ELASTIC_BATCH_SIZE = 50;
    
    private ProdRefsBatch batch = new ProdRefsBatch();
    private RefsDocWriter writer;
    private boolean primaryOnly;
    
    
    /**
     * Constructor
     * @param writer JSON or XML document writer.
     * @param primaryOnly if true, only process primary references
     */
    public CollectionInventoryProcessor(RefsDocWriter writer, boolean primaryOnly)
    {
        log = LogManager.getLogger(this.getClass());
        this.writer = writer;
        this.primaryOnly = primaryOnly;
    }
    
    
    /**
     * Parse collection inventory file, e.g., "document_collection_inventory.csv",
     * extract primary and secondary references (lidvids) and write extracted data
     * into a JSON or XML file. JSON files can be imported into Elasticsearch by 
     * Registry Manager tool.
     * 
     * @param meta Collection metadata
     * @param inventoryFile Collection inventory file, e.g., "document_collection_inventory.csv"
     * @param cacheProductIds if true, cache product lidvids
     * @throws Exception Generic exception
     */
    public void writeCollectionInventory(Metadata meta, File inventoryFile, boolean cacheProductIds) throws Exception
    {
        writePrimaryRefs(meta, inventoryFile, cacheProductIds);
        if(!primaryOnly) writeSecondaryRefs(meta, inventoryFile);
    }
    
    
    /**
     * Write primary product references
     * @param meta Collection metadata
     * @param inventoryFile Collection inventory file, e.g., "document_collection_inventory.csv"
     * @param cacheProductIds if true, cache product lidvids
     * @throws Exception Generic exception
     */
    private void writePrimaryRefs(Metadata meta, File inventoryFile, boolean cacheProductIds) throws Exception
    {
        batch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        
        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), RefType.PRIMARY);
        
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
            writer.writeBatch(meta, batch, RefType.PRIMARY);
            
            if(count < WRITE_BATCH_SIZE) break;
        }
        
        rd.close();
    }

    
    /**
     * Write secondary product references
     * @param meta Collection metadata 
     * @param inventoryFile Collection inventory file, e.g., "document_collection_inventory.csv"
     * @throws Exception Generic exception
     */
    private void writeSecondaryRefs(Metadata meta, File inventoryFile) throws Exception
    {
        batch.batchNum = 0;
        
        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), RefType.SECONDARY);
        
        while(true)
        {
            int count = rd.readNextBatch(WRITE_BATCH_SIZE, batch);
            if(count == 0) break;
            
            // Write batch
            writer.writeBatch(meta, batch, RefType.SECONDARY);
            
            if(count < WRITE_BATCH_SIZE) break;
        }
        
        rd.close();
    }
    
    
    /**
     * Query Registry (Elasticsearch) to find existing (registered) products.
     * Cache only non-registered products. This method uses "RefsCache" singleton.
     * 
     * @param meta Collection metadata
     * @param inventoryFile Collection inventory file, e.g., "document_collection_inventory.csv"
     * @throws Exception Generic exception
     */
    public void cacheNonRegisteredInventory(Metadata meta, File inventoryFile) throws Exception
    {
        if(RegistryManager.getInstance() == null) throw new Exception("Registry is not configured");

        batch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        RegistryDAO dao = RegistryManager.getInstance().getRegistryDAO(); 

        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), RefType.PRIMARY);
        
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
