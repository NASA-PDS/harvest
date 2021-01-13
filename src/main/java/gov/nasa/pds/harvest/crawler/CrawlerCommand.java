package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.meta.LidVidMap;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.harvest.util.out.DocWriter;
import gov.nasa.pds.harvest.util.out.EsDocWriter;
import gov.nasa.pds.harvest.util.out.SolrDocWriter;


public class CrawlerCommand
{
    private Logger log;
    private Configuration cfg;
    private DocWriter writer;
    
    private Counter counter;
    private BundleProcessor bundleProc;
    private CollectionProcessor colProc;
    private ProductProcessor prodProc;
    
    
    public CrawlerCommand()
    {
        log = LogManager.getLogger(this.getClass());
    }


    public void run(CommandLine cmdLine) throws Exception
    {
        configure(cmdLine);

        for(BundleCfg bCfg: cfg.bundles)
        {
            processBundle(bCfg);
        }
        
        writer.close();
        
        printSummary();
    }
    
    
    private void configure(CommandLine cmdLine) throws Exception
    {
        // Output directory
        String outDir = cmdLine.getOptionValue("o", "/tmp/harvest/out");
        log.log(LogUtils.LEVEL_SUMMARY, "Output directory: " + outDir);
        File fOutDir = new File(outDir);
        fOutDir.mkdirs();

        // Output format
        String outFormat = cmdLine.getOptionValue("f", "json").toLowerCase();
        log.log(LogUtils.LEVEL_SUMMARY, "Output format: " + outFormat);

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
        File cfgFile = new File(cmdLine.getOptionValue("c"));
        log.log(LogUtils.LEVEL_SUMMARY, "Reading configuration from " + cfgFile.getAbsolutePath());
        cfg = ConfigReader.read(cfgFile);

        // Xpath maps
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(cfg.xpathMaps);
        
        // Processors
        counter = new Counter();
        bundleProc = new BundleProcessor(cfg, writer, counter);
        colProc = new CollectionProcessor(cfg, writer, counter);
        prodProc = new ProductProcessor(cfg, writer, counter);
    }


    private void processBundle(BundleCfg bCfg) throws Exception
    {
        File rootDir = new File(bCfg.dir);
        if(!rootDir.exists()) 
        {
            log.warn("Invalid bundle directory: " + rootDir.getAbsolutePath());
            return;
        }
        
        log.info("Processing bundle directory " + rootDir.getAbsolutePath());
        
        // Process bundles
        bundleProc.process(bCfg);
        LidVidMap colToBundleMap = bundleProc.getCollectionToBundleMap();
        
        // Process collections
        colProc.process(rootDir, colToBundleMap);
        
        // Process products
        prodProc.process(rootDir);
    }
    
    
    private void printSummary()
    {
        log.log(LogUtils.LEVEL_SUMMARY, "Summary:");
        int processedCount = counter.prodCounters.getTotal();
        
        log.log(LogUtils.LEVEL_SUMMARY, "Skipped files: " + counter.skippedFileCount);
        log.log(LogUtils.LEVEL_SUMMARY, "Processed files: " + processedCount);
        
        if(processedCount > 0)
        {
            log.log(LogUtils.LEVEL_SUMMARY, "File counts by type:");
            for(CounterMap.Item item: counter.prodCounters.getCounts())
            {
                log.log(LogUtils.LEVEL_SUMMARY, "  " + item.name + ": " + item.count);
            }
            
            log.log(LogUtils.LEVEL_SUMMARY, "Package ID: " + PackageIdGenerator.getInstance().getPackageId());
        }
    }

}
