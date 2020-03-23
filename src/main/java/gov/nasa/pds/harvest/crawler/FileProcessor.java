package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;
import org.apache.tika.Tika;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.util.Counter;
import gov.nasa.pds.harvest.util.ExceptionUtils;
import gov.nasa.pds.harvest.util.xml.XmlStreamUtils;


public class FileProcessor implements ProductCrawler.Callback
{
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());

    private Configuration cfg;
    private MetadataProcessor metaProcessor;
    private XmlStreamUtils xmlUtils;
    
    private Tika tika;
    
    private Counter counter;
    private int totalFileCount;
    

    public FileProcessor(File outDir, Configuration cfg) throws Exception
    {
        this.cfg = cfg;
        xmlUtils = new XmlStreamUtils();
        counter = new Counter();
        tika = new Tika();
        metaProcessor = new MetadataProcessor(outDir, cfg);
    }
    
    
    public int getTotalFileCount()
    {
        return totalFileCount;
    }
    
    
    public Counter getCounter()
    {
        return counter;
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
            LOG.severe(ExceptionUtils.getMessage(ex));
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
            return;
        }

        // Apply product filter
        if(includeXmlFile(file))
        {
            metaProcessor.process(file, counter);
        }
    }

    
    // Apply product filter
    private boolean includeXmlFile(File file) throws Exception
    {
        if(cfg.directories.prodFilterIncludes != null)
        {
            String rootElement = xmlUtils.getRootElement(file);
            if(rootElement == null) 
            {
                LOG.warning("Invalid XML file: " + file.getAbsolutePath());
                return false;
            }
            return cfg.directories.prodFilterIncludes.contains(rootElement);
        }
        else if(cfg.directories.prodFilterExcludes != null)
        {
            String rootElement = xmlUtils.getRootElement(file);
            if(rootElement == null) 
            {
                LOG.warning("Invalid XML file: " + file.getAbsolutePath());
                return false;
            }
            return !cfg.directories.prodFilterExcludes.contains(rootElement);
        }
        else
        {
            return true;
        }
    }
}
