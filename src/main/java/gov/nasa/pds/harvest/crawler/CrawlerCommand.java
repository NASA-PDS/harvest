package gov.nasa.pds.harvest.crawler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.harvest.util.out.RefsDocWriter;
import gov.nasa.pds.harvest.util.out.RefsDocWriterJson;
import gov.nasa.pds.harvest.util.out.RefsDocWriterXml;
import gov.nasa.pds.harvest.util.out.RegistryDocWriter;
import gov.nasa.pds.harvest.util.out.RegistryDocWriterJson;
import gov.nasa.pds.harvest.util.out.RegistryDocWriterXml;


/**
 * A CLI command to crawl files and process PDS4 labels.  
 * 
 * @author karpenko
 */
public class CrawlerCommand
{
    private Logger log;
    private Configuration cfg;
    private RegistryDocWriter regWriter;
    private RefsDocWriter refsWriter;
    
    // Processors
    private Counter counter;
    private DirsProcessor dirsProc;
    private BundleProcessor bundleProc;
    private CollectionProcessor colProc;
    private ProductProcessor prodProc;
    
    
    /**
     * Constructor
     */
    public CrawlerCommand()
    {
        log = LogManager.getLogger(this.getClass());
    }


    /**
     * Run this command.
     * @param cmdLine Apache Commons CLI library's class 
     * containing parsed command line parameters.
     * @throws Exception Generic exception
     */
    public void run(CommandLine cmdLine) throws Exception
    {
        configure(cmdLine);

        RegistryManager.init(cfg.registryCfg);

        if(cfg.dirs != null)
        {
            processDirs();
        }
        else if(cfg.bundles != null)
        {
            processBundles();
        }
                
        regWriter.close();
        refsWriter.close();
        RegistryManager.destroy();
        
        printSummary();
    }
    
    
    /**
     * Process bundles configured in Harvest configuration file. 
     * @throws Exception Generic exception
     */
    private void processBundles() throws Exception
    {
        for(BundleCfg bCfg: cfg.bundles)
        {
            processBundle(bCfg);
        }
    }
    

    /**
     * Process directories configured in Harvest configuration file. 
     * @throws Exception Generic exception
     */
    private void processDirs() throws Exception
    {
        for(String path: cfg.dirs)
        {
            processDirectory(path);
        }
    }
    
    
    /**
     * Parse command-line parameters and configuration file to initialize
     * logger, data writers, data processors, etc.
     * @param Apache Commons CLI library's class 
     * containing parsed command line parameters.
     * @throws Exception Generic exception
     */
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
            regWriter = new RegistryDocWriterXml(fOutDir);
            refsWriter = new RefsDocWriterXml(fOutDir);
            break;
        case "json":
            regWriter = new RegistryDocWriterJson(fOutDir);
            refsWriter = new RefsDocWriterJson(fOutDir);
            break;
        default:
            throw new Exception("Invalid output format " + outFormat);                
        }
        
        // Configuration file
        File cfgFile = new File(cmdLine.getOptionValue("c"));
        log.log(LogUtils.LEVEL_SUMMARY, "Reading configuration from " + cfgFile.getAbsolutePath());
        ConfigReader cfgReader = new ConfigReader();
        cfg = cfgReader.read(cfgFile);

        if(cfg.registryCfg == null)
        {
            log.warn("Registry is not configured");
        }

        // Xpath maps
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(cfg.xpathMaps);
        
        // Processors
        counter = new Counter();

        if(cfg.dirs != null)
        {
            dirsProc = new DirsProcessor(cfg, regWriter, refsWriter, counter);
        }
        else if(cfg.bundles != null)
        {
            bundleProc = new BundleProcessor(cfg, regWriter, counter);
            colProc = new CollectionProcessor(cfg, regWriter, refsWriter, counter);
            prodProc = new ProductProcessor(cfg, regWriter, counter);
        }
    }

    
    private void processDirectory(String path) throws Exception
    {
        File rootDir = new File(path);
        if(!rootDir.exists())
        {
            log.warn("Invalid path: " + rootDir.getAbsolutePath());
            return;
        }
        
        log.info("Processing directory: " + rootDir.getAbsolutePath());
        
        dirsProc.process(rootDir);
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

        // Clear reference cache
        RefsCache.getInstance().getCollectionRefsCache().clear();
        RefsCache.getInstance().getProdRefsCache().clear();
        
        // Process bundles
        int count = bundleProc.process(bCfg);
        if(count == 0)
        {
            log.warn("No bundles found in " + rootDir.getAbsolutePath());
            return;
        }
        
        // Process collections
        count = colProc.process(bCfg);
        if(count == 0)
        {
            log.warn("No collections found in " + rootDir.getAbsolutePath());
            return;
        }
        
        // Process products
        prodProc.process(bCfg);
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
