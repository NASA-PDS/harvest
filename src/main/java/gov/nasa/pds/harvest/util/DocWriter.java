package gov.nasa.pds.harvest.util;

import gov.nasa.pds.harvest.meta.FileData;
import gov.nasa.pds.harvest.meta.Metadata;


public interface DocWriter
{
    public void write(FileData fileData, Metadata meta) throws Exception;
    public void close() throws Exception;
}
