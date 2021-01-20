package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.meta.Metadata;


public interface RegistryDocWriter
{
    public void write(Metadata meta) throws Exception;
    public void close() throws Exception;
}
