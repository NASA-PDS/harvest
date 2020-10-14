package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.DocWriter;
import gov.nasa.pds.harvest.util.ExceptionUtils;
import gov.nasa.pds.harvest.util.xml.XmlStreamUtils;


public class FileProcessor implements ProductCrawler.Callback
{
    private Logger LOG;

    // Will skip files longer than 10MB
    private static final long MAX_XML_FILE_LENGTH = 10_000_000;
    
    private Configuration cfg;
    private MetadataProcessor metaProcessor;
    private XmlStreamUtils xmlUtils;
    
    private Tika tika;
    
    private Counter counter;
    
    // Stopped on error
    private boolean stoppedOnError = false;

    
    public FileProcessor(Configuration cfg, DocWriter writer) throws Exception
    {
        LOG = LogManager.getLogger(getClass());
        
        this.cfg = cfg;
        
        xmlUtils = new XmlStreamUtils();
        counter = new Counter();
        tika = new Tika();
        
        metaProcessor = new MetadataProcessor(writer, cfg);
    }
    
    
    public boolean stoppedOnError()
    {
        return stoppedOnError;
    }
    
    
    public int getSkippedFileCount()
    {
        return counter.skippedFileCount;
    }
    
    
    public CounterMap getProdTypeCounter()
    {
        return counter.prodCounters;
    }
    
    
    @Override
    public boolean onFile(File file)
    {
        try
        {
            processFile(file);
            return true;
        }
        catch(Exception ex)
        {
            LOG.error(ExceptionUtils.getMessage(ex));
            counter.skippedFileCount++;

            stoppedOnError = true;
            return false;
        }
    }

    
    public void close() throws Exception
    {
        metaProcessor.close();
    }

    
    private void processFile(File file) throws Exception
    {
        String mimeType = tika.detect(file);
        if("application/xml".equals(mimeType))
        {
            processXmlFile(file);
        }
        else
        {
            LOG.warn("Unsupported MIME type: " + mimeType + " (" + file.toURI().getPath() + ")");
            counter.skippedFileCount++;
        }
    }
    

    private void processXmlFile(File file) throws Exception
    {
        // More than 1 megabyte. Too big for a label.
        if(file.length() > MAX_XML_FILE_LENGTH)
        {
            LOG.warn("File is too big to parse: " + file.toURI().getPath());
            counter.skippedFileCount++;
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
                LOG.warn("Invalid XML file: " + file.getAbsolutePath());
                counter.skippedFileCount++;
                return false;
            }
            
            return cfg.directories.prodFilterIncludes.contains(rootElement);
        }
        else if(cfg.directories.prodFilterExcludes != null)
        {
            String rootElement = xmlUtils.getRootElement(file);
            if(rootElement == null) 
            {
                LOG.warn("Invalid XML file: " + file.getAbsolutePath());
                counter.skippedFileCount++;
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
