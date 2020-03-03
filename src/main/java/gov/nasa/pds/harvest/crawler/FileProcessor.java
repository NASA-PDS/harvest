package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;

import org.apache.tika.Tika;

import gov.nasa.pds.harvest.meta.MetadataExtractor;
import gov.nasa.pds.harvest.meta.RegistryMetadata;
import gov.nasa.pds.harvest.util.FileData;
import gov.nasa.pds.harvest.util.FileDataUtils;
import gov.nasa.pds.harvest.util.SolrDocWriter;


public class FileProcessor implements ProductCrawler.Callback
{
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
    private Tika tika;
    private SolrDocWriter writer;
    private MetadataExtractor metaExtractor;

    
    public FileProcessor(File outDir) throws Exception
    {
        tika = new Tika();
        writer = new SolrDocWriter(outDir);
        metaExtractor = new MetadataExtractor();
    }
    
    
    @Override
    public void onFile(File file)
    {
        try
        {
            processFile(file);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    
    public void close()
    {
        try { writer.close(); } catch(Exception ex) {}
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
        FileData fd = new FileData(file, "application/xml");

        // More than 1 megabyte. Too big for a label.
        if(fd.size > 1000000)
        {
            LOG.warning("File is too big to parse: " + file.toURI().getPath());
        }
        else
        {
            RegistryMetadata meta = metaExtractor.extract(file);
            if(!validateAndContinue(meta, file)) return;
            
            FileDataUtils.setFileContent(fd, file);
            writer.write(fd, meta);
        }
    }

    
    private boolean validateAndContinue(RegistryMetadata meta, File file)
    {
        if(meta.lid == null)
        {
            LOG.severe("Missing logical identifier: " + file.toURI().getPath());
            return false;
        }

        if(meta.vid == null)
        {
            LOG.severe("Missing version id: " + file.toURI().getPath());
            return false;
        }
        
        return true;
    }
    

}
