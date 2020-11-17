package gov.nasa.pds.harvest.util;

import gov.nasa.pds.harvest.meta.Metadata;


public interface DocWriter
{
    public void write(Metadata meta) throws Exception;
    public void close() throws Exception;
}
