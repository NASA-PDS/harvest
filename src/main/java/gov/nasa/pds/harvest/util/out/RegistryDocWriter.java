package gov.nasa.pds.harvest.util.out;

import java.io.Closeable;

import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.xml.XmlNamespaces;

/**
 * Interface to write metadata extracted from PDS4 label.
 *  
 * @author karpenko
 */
public interface RegistryDocWriter extends Closeable
{
    /**
     * Write metadata extracted from PDS4 labels.
     * @param meta metadata extracted from PDS4 label.
     * @param nsInfo namespace information from the XML
     * @throws Exception Generic exception
     */
    public void write(Metadata meta, XmlNamespaces nsInfo) throws Exception;
}
