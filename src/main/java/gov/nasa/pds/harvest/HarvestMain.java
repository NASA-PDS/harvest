package gov.nasa.pds.harvest;

import java.io.File;

import gov.nasa.pds.harvest.cfg.policy.PolicyReader;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.crawler.FileProcessor;
import gov.nasa.pds.harvest.crawler.ProductCrawler;
import gov.nasa.pds.harvest.log.LogUtils;


public class HarvestMain
{
    public static void main(String[] args)
    {
        HarvestCli cli = new HarvestCli();
        
        if(args.length == 0)
        {
            cli.printHelp();
            System.exit(1);
        }

        if(!cli.parse(args))
        {
            System.out.println(cli.getError());
            System.exit(1);
        }
        
        initLogger(cli);
        if(!loadConfiguration(cli)) return;
        
        runCrawler(cli, null);
    }


    private static void initLogger(HarvestCli cli)
    {
        String verbosity = cli.getOptionValue("v", "1");
        String logFile = cli.getOptionValue("l");
        
        LogUtils.setupLogger(verbosity, logFile);
    }
 
    
    private static boolean loadConfiguration(HarvestCli cli)
    {
        String configFile = cli.getOptionValue("c");
        PolicyReader rd = new PolicyReader();
        //Policy policy = rd.read(new File(configFile));

        
        return true;
    }
    
    
    private static void runCrawler(HarvestCli cli, Policy policy)
    {

        try
        {
            String outDir = cli.getOptionValue("o", "/tmp/harvest/solr");
            File dir = new File(outDir);
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
