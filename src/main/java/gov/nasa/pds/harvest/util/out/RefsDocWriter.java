package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.crawler.ProdRefsBatch;
import gov.nasa.pds.harvest.crawler.RefType;
import gov.nasa.pds.harvest.meta.Metadata;


/**
 * Interface to write product references extracted from PDS4 collection inventory files.
 * 
 * @author karpenko
 */
public interface RefsDocWriter
{
    /**
     * Write a batch of product references.
     * @param meta product metadata extracted from PDS4 collection label.
     * @param batch A batch of product references extracted from PDS4 collection inventory files.
     * @param refType product reference type: primary or secondary.
     * @throws Exception
     */
    public void writeBatch(Metadata meta, ProdRefsBatch batch, RefType refType) throws Exception;
    
    /**
     * Close resources / output file.
     * @throws Exception
     */
    public void close() throws Exception;
}
