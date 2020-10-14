package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.DocWriter;
import gov.nasa.pds.harvest.util.HarvestLogManager;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.out.EsDocWriter;
import gov.nasa.pds.harvest.util.out.SolrDocWriter;


public class CrawlerCommand
{
    private Logger minLogger; 
    
    
    public CrawlerCommand()
    {
        minLogger = HarvestLogManager.getMinInfoLogger();
    }

    
    public void run(CommandLine cmdLine) throws Exception
    {
        // Output directory
        String outDir = cmdLine.getOptionValue("o", "/tmp/harvest/out");
        minLogger.info("Output directory: " + outDir);
        File fOutDir = new File(outDir);
        fOutDir.mkdirs();

        // Output format
        String outFormat = cmdLine.getOptionValue("f", "json").toLowerCase();
        minLogger.info("Output format: " + outFormat);

        DocWriter writer = null;
        
        switch(outFormat)
        {
        case "xml":
            writer = new SolrDocWriter(fOutDir);
            break;
        case "json":
            writer = new EsDocWriter(fOutDir);
            break;
        default:
            throw new Exception("Invalid output format " + outFormat);                
        }
        
        // Configuration file
        Configuration cfg = loadConfiguration(cmdLine.getOptionValue("c"));
        
        // Run crawler
        runCrawler(cfg, writer);
    }
    

    private Configuration loadConfiguration(String pConfigFile) throws Exception
    {
        File cfgFile = new File(pConfigFile);
        minLogger.info("Reading configuration from " + pConfigFile);
        
        // Read config file
        ConfigReader rd = new ConfigReader();
        Configuration cfg = rd.read(cfgFile);
        
        // Load xpath maps from files
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(cfg.xpathMaps);
        
        return cfg;
    }
    
    
    private void runCrawler(Configuration cfg, DocWriter writer) throws Exception
    {
        FileProcessor fileProcessor = new FileProcessor(cfg, writer);
        ProductCrawler crawler = new ProductCrawler(cfg.directories, fileProcessor);
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
