package gov.nasa.pds.harvest.crawler;

import java.io.File;
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
    
    
    public CrawlerCommand(HarvestCli cli)
    {
        pConfigFile = cli.getOptionValue("c");
        pOutDir = cli.getOptionValue("o", "/tmp/harvest/solr");
    }

    
    public int run()
    {
        LOG.info("Starting product crawler...");
    
        //setHomeDir();
        
        if(!loadConfiguration()) return 1;
        
        return 0;
    }
    

    private void setHomeDir()
    {
        String home = System.getenv("HARVEST_HOME");
        if(home == null)
        {
            home = System.getProperty("user.dir");
            LOG.warning("HARVEST_HOME environment variable is not set. Will use " + home);
        }
        else
        {
            LOG.info("HARVEST_HOME = " + home);
        }
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
            LOG.severe(ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    
    private void runCrawler(HarvestCli cli, Policy policy)
    {

        try
        {
            File dir = new File(pOutDir);
            dir.mkdirs();
            
            
            FileProcessor cb = new FileProcessor(dir);
            
            ProductCrawler crawler = new ProductCrawler(policy.directories, cb);
            crawler.crawl();
            
            cb.close();
        }
        catch(Exception ex)
        {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(1);
        }
    }

}
