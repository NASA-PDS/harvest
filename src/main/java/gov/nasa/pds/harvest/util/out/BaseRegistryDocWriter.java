package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.Writer;


/**
 * Interface to write metadata extracted from PDS4 label.
 *  
 * @author karpenko
 */
public abstract class BaseRegistryDocWriter implements RegistryDocWriter
{
    protected File outDir;
    protected Writer writer;

    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception Generic exception
     */
    public BaseRegistryDocWriter(File outDir) throws Exception
    {
        this.outDir = outDir;
    }

}
