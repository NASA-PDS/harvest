package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.crawler.ProdRefsBatch;
import gov.nasa.pds.harvest.meta.Metadata;


public interface RefsDocWriter
{
    public void writeBatch(Metadata meta, ProdRefsBatch batch) throws Exception;
    public void close() throws Exception;
}
