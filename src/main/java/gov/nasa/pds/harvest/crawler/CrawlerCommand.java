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


public class CrawlerCommand
{
    private Logger log;
    private Configuration cfg;
    private RegistryDocWriter regWriter;
    private RefsDocWriter refsWriter;
    
    private Counter counter;
    private BundleProcessor bundleProc;
    private CollectionProcessor colProc;
    private ProductProcessor prodProc;
    
    private int bundleCount;
    
    
    public CrawlerCommand()
    {
        log = LogManager.getLogger(this.getClass());
    }


    public void run(CommandLine cmdLine) throws Exception
    {
        configure(cmdLine);

        RegistryManager.init(cfg.registryCfg);

        for(BundleCfg bCfg: cfg.bundles)
        {
            processBundle(bCfg);
        }
        
        regWriter.close();
        refsWriter.close();
        RegistryManager.destroy();
        
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
        bundleProc = new BundleProcessor(cfg, regWriter, counter);
        colProc = new CollectionProcessor(cfg, regWriter, refsWriter, counter);
        prodProc = new ProductProcessor(cfg, regWriter, counter);
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
        
        this.bundleCount += count;
        
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
        if(this.bundleCount == 0) return;
        
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
