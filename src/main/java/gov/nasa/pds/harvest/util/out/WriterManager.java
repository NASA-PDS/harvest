package gov.nasa.pds.harvest.util.out;

import java.io.File;

import gov.nasa.pds.harvest.util.CloseUtils;


/**
 * Singleton to hold references to registry metadata, collection inventory (product references)
 * and other data writers.
 * 
 * @author karpenko
 */
public class WriterManager
{
    private static WriterManager instance;
    
    private RegistryDocWriter regWriter;
    private RefsDocWriter refsWriter;
    private SupplementalWriter supWriter;

   
    /**
     * Provate constructor. Use getInstance() instead.
     */
    private WriterManager(File outDir) throws Exception
    {
        supWriter = new SupplementalWriter(outDir);
    }

    
    /**
     * Create writers to write JSON data files with metadata
     * extracted from PDS4 labels. Generated JSON files can be imported 
     * into Elasticsearch by Registry manager tool. 
     * @param outDir output directory
     * @throws Exception an exception
     */
    public static void initJson(File outDir) throws Exception
    {
        if(instance != null) throw new Exception("WriterManager is already initialized.");
        
        instance = new WriterManager(outDir);
        instance.regWriter = new RegistryDocWriterJson(outDir);
        instance.refsWriter = new RefsDocWriterJson(outDir);
    }

    
    /**
     * Create writers to write XML data files with metadata
     * extracted from PDS4 labels. 
     * @param outDir output directory
     * @throws Exception an exception
     */
    public static void initXml(File outDir) throws Exception
    {
        instance = new WriterManager(outDir);
        instance.regWriter = new RegistryDocWriterXml(outDir);
        instance.refsWriter = new RefsDocWriterXml(outDir);
    }

    
    /**
     * Clean up resources / close files.
     */
    public static void destroy()
    {
        if(instance == null) return;
        
        CloseUtils.close(instance.regWriter);
        CloseUtils.close(instance.refsWriter);
        CloseUtils.close(instance.supWriter);
    }

    
    /**
     * Get singleton instance.
     * @return the singleton instance.
     * @throws Exception an exception.
     */
    public static WriterManager getInstance() throws Exception
    {
        if(instance == null) throw new Exception("WriterManager is not initialized.");
        
        return instance;
    }
    
    
    /**
     * Get registry metadata writer.
     * @return registry metadata writer
     */
    public RegistryDocWriter getRegistryWriter()
    {
        return regWriter;
    }

    
    /**
     * Get collection inventory (product references) writer
     * @return collection inventory (product references) writer
     */
    public RefsDocWriter getRefsWriter()
    {
        return refsWriter;
    }

    
    /**
     * Get a writer for supplemental products
     * @return a writer for supplemental products
     */
    public SupplementalWriter getSupplementalWriter()
    {
        return supWriter;
    }
}
