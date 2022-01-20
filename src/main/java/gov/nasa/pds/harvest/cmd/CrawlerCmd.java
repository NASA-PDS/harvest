package gov.nasa.pds.harvest.cmd;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.crawler.BundleProcessor;
import gov.nasa.pds.harvest.crawler.CollectionProcessor;
import gov.nasa.pds.harvest.crawler.Counter;
import gov.nasa.pds.harvest.crawler.FilesProcessor;
import gov.nasa.pds.harvest.crawler.ProductProcessor;
import gov.nasa.pds.harvest.crawler.RefsCache;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.dao.SchemaUtils;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.harvest.util.out.WriterManager;


/**
 * A CLI command to crawl files and process PDS4 labels.  
 * 
 * @author karpenko
 */
public class CrawlerCmd implements CliCommand
{
    private Logger log;
    private Configuration cfg;
    
    // Processors
    private Counter counter;
    private FilesProcessor filesProc;
    private BundleProcessor bundleProc;
    private CollectionProcessor colProc;
    private ProductProcessor prodProc;
    
    
    /**
     * Constructor
     */
    public CrawlerCmd()
    {
        log = LogManager.getLogger(this.getClass());
    }


    /**
     * Run this command.
     * @param cmdLine Apache Commons CLI library's class 
     * containing parsed command line parameters.
     * @throws Exception Generic exception
     */
    @Override
    public void run(CommandLine cmdLine) throws Exception
    {
        configure(cmdLine);

        RegistryManager.init(cfg.registryCfg);
        log.info("Reading registry schema from Elasticsearch");
        SchemaUtils.updateFieldsCache();

        try
        {
            if(cfg.dirs != null)
            {
                processDirs();
            }
            else if(cfg.bundles != null)
            {
                processBundles();
            }
            else if(cfg.manifests != null)
            {
                processManifests();
            }
        }
        finally
        {
            WriterManager.destroy();
            RegistryManager.destroy();
        }
        
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
     * Process manifests (lists of files) configured in Harvest configuration file. 
     * @throws Exception Generic exception
     */
    private void processManifests() throws Exception
    {
        for(String path: cfg.manifests)
        {
            processManifest(path);
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
            WriterManager.initXml(fOutDir);
            break;
        case "json":
            WriterManager.initJson(fOutDir);
            break;
        default:
            throw new Exception("Invalid output format " + outFormat);                
        }
        
        // Configuration file
        File cfgFile = new File(cmdLine.getOptionValue("c"));
        log.log(LogUtils.LEVEL_SUMMARY, "Reading configuration from " + cfgFile.getAbsolutePath());
        ConfigReader cfgReader = new ConfigReader();
        cfg = cfgReader.read(cfgFile);

        if(cfg.bundles != null && cfg.registryCfg == null)
        {
            log.warn("Registry (Elasticsearch) is not configured. "
                    + "Registered products will be processed again.");
        }

        // Xpath maps
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(cfg.xpathMaps);
        
        // Processors
        counter = new Counter();

        if(cfg.dirs != null || cfg.manifests != null)
        {
            filesProc = new FilesProcessor(cfg, counter);
        }
        else if(cfg.bundles != null)
        {
            bundleProc = new BundleProcessor(cfg, counter);
            colProc = new CollectionProcessor(cfg, counter);
            prodProc = new ProductProcessor(cfg, counter);
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
        
        filesProc.processDirectory(rootDir);
    }
    

    private void processManifest(String path) throws Exception
    {
        File manifestFile = new File(path);
        log.info("Processing manifest file: " + manifestFile.getAbsolutePath());        
        
        if(!manifestFile.exists())
        {
            throw new Exception("Invalid manifest path: " + manifestFile.getAbsolutePath());
        }
        
        filesProc.processManifest(manifestFile);
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
