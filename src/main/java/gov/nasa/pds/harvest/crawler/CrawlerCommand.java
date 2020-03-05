package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.pds.harvest.HarvestCli;
import gov.nasa.pds.harvest.cfg.policy.PolicyReader;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;


public class CrawlerCommand
{
    private static final Logger LOG = Logger.getLogger(CrawlerCommand.class.getName());
    
    private String pConfigFile;
    private String pOutDir;
    private Policy policy;
    private boolean storeBlob = false;

    
    public CrawlerCommand(HarvestCli cli)
    {
        pConfigFile = cli.getOptionValue("c");
        pOutDir = cli.getOptionValue("o", "/tmp/harvest/solr");
    }

    
    public int run()
    {
        LOG.info("Starting product crawler...");
    
        if(!loadConfiguration()) return 1;
        runCrawler();
        
        return 0;
    }
    

    private boolean loadConfiguration()
    {
        File cfgFile = new File(pConfigFile);
        LOG.info("Reading configuration from " + pConfigFile);
        
        try
        {
            // Read config file
            PolicyReader rd = new PolicyReader();
            policy = rd.read(cfgFile);
            
            // Load xpath maps from files
            XPathCacheLoader xpcLoader = new XPathCacheLoader();
            xpcLoader.load(policy.xpathMaps);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE, "", ex);
            return false;
        }
        
        return true;
    }
    
    
    private void runCrawler()
    {
        try
        {
            LOG.info("Will write Solr docs to " + pOutDir);
            File dir = new File(pOutDir);
            dir.mkdirs();
            
            FileProcessor cb = new FileProcessor(dir, storeBlob);
            ProductCrawler crawler = new ProductCrawler(policy.directories, cb);
            crawler.crawl();
            cb.close();
            
            LOG.info("Total file count: " + cb.getTotalFileCount());
            LOG.info("Processed file count: " + cb.getProcessedFileCount());
        }
        catch(Exception ex)
        {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(1);
        }
    }

}
