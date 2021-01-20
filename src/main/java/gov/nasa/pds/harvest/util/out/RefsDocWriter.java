package gov.nasa.pds.harvest.util.out;

import java.io.File;

import gov.nasa.pds.harvest.meta.Metadata;


public interface RefsDocWriter
{
    public void writeCollectionInventory(Metadata meta, File inventoryFile) throws Exception;
    public void close() throws Exception;
}
