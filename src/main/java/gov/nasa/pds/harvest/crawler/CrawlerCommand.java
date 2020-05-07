package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.HarvestLogManager;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class CrawlerCommand
{
    private Configuration policy;
    private Logger minLogger; 
    
    
    public CrawlerCommand()
    {
        minLogger = HarvestLogManager.getMinInfoLogger();
    }

    
    public void run(CommandLine cmdLine) throws Exception
    {
        loadConfiguration(cmdLine.getOptionValue("c"));
        
        String outDir = cmdLine.getOptionValue("o", "/tmp/harvest/solr");
        runCrawler(outDir);
    }
    

    private void loadConfiguration(String pConfigFile) throws Exception
    {
        File cfgFile = new File(pConfigFile);
        minLogger.info("Reading configuration from " + pConfigFile);
        
        // Read config file
        ConfigReader rd = new ConfigReader();
        policy = rd.read(cfgFile);
        
        // Load xpath maps from files
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(policy.xpathMaps);
    }
    
    
    private void runCrawler(String pOutDir) throws Exception
    {
        minLogger.info("Will write Solr docs to " + pOutDir);
        File outDir = new File(pOutDir);
        outDir.mkdirs();
        
        FileProcessor fileProcessor = new FileProcessor(outDir, policy);
        ProductCrawler crawler = new ProductCrawler(policy.directories, fileProcessor);
        crawler.crawl();
        fileProcessor.close();

        if(!fileProcessor.stoppedOnError())
        {
            printSummary(fileProcessor);
        }
    }
    
    
    private void printSummary(FileProcessor cb)
    {
        minLogger.info("Summary:");
        int processedCount = cb.getProdTypeCounter().getTotal();
        
        minLogger.info("Skipped files: " + cb.getSkippedFileCount());
        minLogger.info("Processed files: " + processedCount);
        
        if(processedCount > 0)
        {
            minLogger.info("File counts by type:");
            for(CounterMap.Item item: cb.getProdTypeCounter().getCounts())
            {
                minLogger.info("  " + item.name + ": " + item.count);
            }
            
            minLogger.info("Package ID: " + PackageIdGenerator.getInstance().getPackageId());
        }
    }

}
