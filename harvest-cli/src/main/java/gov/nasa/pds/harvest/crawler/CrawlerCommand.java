package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.pds.harvest.HarvestCli;
import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.Counter;
import gov.nasa.pds.harvest.util.ExceptionUtils;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class CrawlerCommand
{
    private static final Logger LOG = Logger.getLogger(CrawlerCommand.class.getName());
    
    private String pConfigFile;
    private String pOutDir;
    private Configuration policy;

    
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
            ConfigReader rd = new ConfigReader();
            policy = rd.read(cfgFile);
            
            // Load xpath maps from files
            XPathCacheLoader xpcLoader = new XPathCacheLoader();
            xpcLoader.load(policy.xpathMaps);
        }
        catch(Exception ex)
        {
            LOG.severe(ExceptionUtils.getMessage(ex));
            return false;
        }
        
        return true;
    }
    
    
    private void runCrawler()
    {
        try
        {
            LOG.info("Will write Solr docs to " + pOutDir);
            File outDir = new File(pOutDir);
            outDir.mkdirs();
            
            FileProcessor cb = new FileProcessor(outDir, policy);
            ProductCrawler crawler = new ProductCrawler(policy.directories, cb);
            crawler.crawl();
            cb.close();
            
            LOG.info("Summary:");
            int processedCount = cb.getCounter().getTotal();
            
            LOG.info("Skipped files: " + (cb.getTotalFileCount() - processedCount));
            LOG.info("Processed files: " + processedCount);
            
            if(processedCount > 0)
            {
                LOG.info("File counts by type:");
                for(Counter.Item item: cb.getCounter().getCounts())
                {
                    LOG.info("  " + item.name + ": " + item.count);
                }
                
                LOG.info("Package ID: " + PackageIdGenerator.getInstance().getPackageId());
            }
        }
        catch(Exception ex)
        {
            System.out.println("ERROR: " + ex.getMessage());
            System.exit(1);
        }
    }

}
