package gov.nasa.pds.harvest.util.out;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import gov.nasa.pds.harvest.util.CloseUtils;

/**
 * Write a list of file paths of supplemental product labels.
 * 
 * @author karpenko
 */
public class SupplementalWriter implements Closeable
{
    private Writer writer;
    
    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception an exception
     */
    public SupplementalWriter(File outDir) throws Exception
    {
        File file = new File(outDir, "supplemental.txt");
        writer = new FileWriter(file);
    }

    
    @Override
    public void close() throws IOException
    {
        CloseUtils.close(writer);
    }
    
    
    /**
     * Write supplemental product label file path
     * @param file supplemental product label file
     * @throws IOException an exception
     */
    public void write(File file) throws IOException
    {
        writer.write(file.getAbsolutePath());
        writer.write("\n");
    }
}
