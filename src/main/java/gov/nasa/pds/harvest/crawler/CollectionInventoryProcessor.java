package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.registry.common.meta.InventoryBatchReader;
import gov.nasa.pds.registry.common.util.doc.ProdRefsBatch;
import gov.nasa.pds.registry.common.util.doc.RefType;


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
    
    private int ELASTIC_BATCH_SIZE = 50;
    
    ProdRefsBatch batch = new ProdRefsBatch();
    
    
    /**
     * Constructor
     * @param primaryOnly if true, only process primary references
     */
    public CollectionInventoryProcessor(boolean primaryOnly)
    {
        log = LogManager.getLogger(this.getClass());
    }
    
    
    /**
     * Query Registry (Elasticsearch) to find existing (registered) products.
     * Cache only non-registered products. This method uses "RefsCache" singleton.
     * 
     * @param inventoryFile Collection inventory file, e.g., "document_collection_inventory.csv"
     * @throws Exception Generic exception
     */
    public void cacheNonRegisteredInventory(File inventoryFile) throws Exception
    {
        if(RegistryManager.getInstance() == null) throw new Exception("Registry is not configured");

        batch.batchNum = 0;
        LidVidCache cache = RefsCache.getInstance().getProdRefsCache();
        RegistryDao dao = RegistryManager.getInstance().getRegistryDao(); 

        InventoryBatchReader rd = new InventoryBatchReader(new FileReader(inventoryFile), RefType.PRIMARY);
        
        while(true)
        {
            int count = rd.readNextBatch(ELASTIC_BATCH_SIZE, batch);
            if(count == 0) break;

            Collection<String> nonRegisteredIds = dao.getNonExistingIds(batch.lidvids);
            
            // Update cache. Only products in cache will be processed.
            cache.addLidVids(nonRegisteredIds);
            cache.addLids(batch.lids);
            
            if(count < ELASTIC_BATCH_SIZE) break;
        }
        
        rd.close();        
    }
    

}
