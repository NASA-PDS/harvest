package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;
import org.apache.tika.Tika;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;


public class FileProcessor implements ProductCrawler.Callback
{
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());

    private MetadataProcessor metaProcessor;
    
    private Tika tika;
    
    private int totalFileCount;
    private int processedFileCount;
    

    public FileProcessor(File outDir, Policy policy) throws Exception
    {
        tika = new Tika();
        metaProcessor = new MetadataProcessor(outDir, policy);
    }
    
    
    public int getTotalFileCount()
    {
        return totalFileCount;
    }
    
    
    public int getProcessedFileCount()
    {
        return processedFileCount;
    }
    
    
    @Override
    public void onFile(File file)
    {
        try
        {
            processFile(file);
            totalFileCount++;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    
    public void close() throws Exception
    {
        metaProcessor.close();
    }

    
    private void processFile(File file) throws Exception
    {
        LOG.info("Processing file " + file.toURI().getPath());
        
        String mimeType = tika.detect(file);
        if("application/xml".equals(mimeType))
        {
            processXmlFile(file);
        }
        else
        {
            LOG.warning("Unsupported MIME type: " + mimeType + " (" + file.toURI().getPath() + ")");
        }
    }
    

    private void processXmlFile(File file) throws Exception
    {
        // More than 1 megabyte. Too big for a label.
        if(file.length() > 1000000)
        {
            LOG.warning("File is too big to parse: " + file.toURI().getPath());
        }
        else
        {
            metaProcessor.process(file);
            processedFileCount++;
        }
    }

    
}
