package gov.nasa.pds.harvest.cmd;

import java.io.File;
import java.security.ProviderException;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.nasa.pds.harvest.Version;
import gov.nasa.pds.harvest.cfg.BundleType;
import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.cfg.HarvestConfigurationType;
import gov.nasa.pds.harvest.crawler.BundleProcessor;
import gov.nasa.pds.harvest.crawler.CollectionProcessor;
import gov.nasa.pds.harvest.crawler.Counter;
import gov.nasa.pds.harvest.crawler.FilesProcessor;
import gov.nasa.pds.harvest.crawler.ProductProcessor;
import gov.nasa.pds.harvest.crawler.RefsCache;
import gov.nasa.pds.harvest.dao.RegistryDocBatch;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.meta.XPathCacheLoader;
import gov.nasa.pds.harvest.util.CounterMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.log.LogUtils;
import gov.nasa.pds.harvest.util.out.WriterManager;
import gov.nasa.pds.registry.common.meta.BasicMetadataExtractor;
import gov.nasa.pds.registry.common.util.ArchiveStatus;


/**
 * A CLI command to crawl files and process PDS4 labels.  
 * 
 * @author karpenko
 */
public class HarvestCmd implements CliCommand
{
    private Logger log;
    private HarvestConfigurationType cfg;
    
    // Processors
    private FilesProcessor filesProc;
    private BundleProcessor bundleProc;
    private CollectionProcessor colProc;
    private ProductProcessor prodProc;
    private String archive_status = BasicMetadataExtractor.DEFAULT_ARCHIVE_STATUS;
    
    /**
     * Constructor
     */
    public HarvestCmd()
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
        if (cmdLine.hasOption("archive-status")) {
          this.archive_status = cmdLine.getOptionValue("archive-status").toLowerCase();
          new ArchiveStatus().validateStatusName(archive_status);;
        }
        configure(cmdLine);

        try
        {
            if(cfg.getLoad().getDirectories() != null)
            {
                processDirs();
            }
            else if(cfg.getLoad().getBundles() != null)
            {
                processBundles();
            }
            else if(cfg.getLoad().getFiles() != null)
            {
                processManifests();
            }
            
            RegistryManager.getInstance().getRegistryWriter().flush();
            printSummary();
        }
        finally
        {
            WriterManager.destroy();
            RegistryManager.destroy();
        }
    }
    
    
    /**
     * Process bundles configured in Harvest configuration file. 
     * @throws Exception Generic exception
     */
    private void processBundles() throws Exception
    {
        for(BundleType bCfg: cfg.getLoad().getBundles().getBundle())
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
        for(String path: cfg.getLoad().getDirectories().getPath())
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
        for(String path: cfg.getLoad().getFiles().getManifest())
        {
            processManifest(path);
        }
    }   
    
    /**
     * Parse command-line parameters and configuration file to initialize
     * logger, data writers, data processors, etc.
     * @param cmdLine Apache Commons CLI library's class 
     * containing parsed command line parameters.
     * @throws Exception Generic exception
     */
    private void configure(CommandLine cmdLine) throws Exception
    {
        // Configuration file
        cfg = readConfigFile(cmdLine);
        
        // Writer manager
        initWriterManager(cmdLine);
        
        boolean overwriteFlag = cmdLine.hasOption("overwrite");
        
        if (!Version.instance().checkVersion(ConfigManager.exchangeRegistry(cfg.getRegistry()),
            Arrays.asList(
                gov.nasa.pds.registry.common.Version.instance(),
                Version.instance()))) {
          throw new ProviderException("Exiting without executing command because version is too old.");
        }
        // Registry manager
        RegistryManager.init(ConfigManager.exchangeRegistry(cfg.getRegistry()), overwriteFlag);
        log.info("Connecting to Elasticsearch");
        RegistryManager.getInstance().getFieldNameCache().update();
        
        // Xpath maps
        XPathCacheLoader xpcLoader = new XPathCacheLoader();
        xpcLoader.load(cfg.getXpathMaps());

        // Processors
        if(cfg.getLoad().getDirectories() != null || cfg.getLoad().getFiles() != null)
        {
            filesProc = new FilesProcessor(cfg, this.archive_status);
        }
        else if(cfg.getLoad().getBundles() != null)
        {
            bundleProc = new BundleProcessor(cfg, this.archive_status);
            colProc = new CollectionProcessor(cfg, this.archive_status);
            prodProc = new ProductProcessor(cfg, this.archive_status);
        }
    }

    
    private void initWriterManager(CommandLine cmdLine) throws Exception
    {
        // Output directory
        String outDir = cmdLine.getOptionValue("o", "/tmp/harvest/out");
        log.log(LogUtils.LEVEL_SUMMARY, "Output directory: " + outDir);
        File fOutDir = new File(outDir);
        fOutDir.mkdirs();

        WriterManager.init(fOutDir);
    }
    
    
    private HarvestConfigurationType readConfigFile(CommandLine cmdLine) throws Exception
    {
        File cfgFile = new File(cmdLine.getOptionValue("c"));
        log.log(LogUtils.LEVEL_SUMMARY, "Reading configuration from " + cfgFile.getAbsolutePath());
        
        HarvestConfigurationType cfg = ConfigManager.read(cfgFile);

        if(!cfg.getFileInfo().isStoreLabels())
        {
            log.warn("XML BLOB storage is disabled "
                    + "(see <fileInfo storeLabels=\"false\"> configuration). "
                    + "Not all Registry features will be available.");
        }

        if(!cfg.getFileInfo().isStoreJsonLabels())
        {
            log.warn("JSON BLOB storage is disabled "
                    + "(see <fileInfo storeJsonLabels=\"false\"> configuration). "
                    + "Not all Registry features will be available.");
        }

        return cfg;
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

    
    private void processBundle(BundleType bCfg) throws Exception
    {
        File rootDir = new File(bCfg.getDir());
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
        Counter counter = RegistryManager.getInstance().getCounter();
        
        RegistryDocBatch.showDuplicates();
        log.log(LogUtils.LEVEL_SUMMARY, "Summary:");
        int processedCount = counter.prodCounters.getTotal();
        
        log.log(LogUtils.LEVEL_SUMMARY, "Skipped files: " + counter.skippedFileCount);
        log.log(LogUtils.LEVEL_SUMMARY, "Loaded files: " + processedCount);
        
        if(processedCount > 0)
        {
            for(CounterMap.Item item: counter.prodCounters.getCounts())
            {
                log.log(LogUtils.LEVEL_SUMMARY, "  " + item.name + ": " + item.count);
            }
        }
        
        log.log(LogUtils.LEVEL_SUMMARY, "Failed files: " + counter.failedFileCount);
        log.log(LogUtils.LEVEL_SUMMARY, "Package ID: " + PackageIdGenerator.getInstance().getPackageId());
    }

}
