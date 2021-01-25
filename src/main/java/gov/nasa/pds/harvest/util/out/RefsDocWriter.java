package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.meta.Metadata;


public interface RefsDocWriter
{
    public void writeBatch(Metadata meta, RefsBatch batch) throws Exception;
    public void close() throws Exception;
}
