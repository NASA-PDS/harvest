package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.meta.Metadata;


/**
 * Interface to write metadata extracted from PDS4 labels.
 *  
 * @author karpenko
 */
public interface RegistryDocWriter
{
    /**
     * Write metadata extracted from PDS4 labels.
     * @param meta
     * @throws Exception
     */
    public void write(Metadata meta) throws Exception;
    
    /**
     * Close resources / output file.
     * @throws Exception
     */
    public void close() throws Exception;
}
