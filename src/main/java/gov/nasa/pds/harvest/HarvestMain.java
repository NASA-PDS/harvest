package gov.nasa.pds.harvest;

import java.io.File;

import gov.nasa.pds.harvest.crawler.FileProcessor;
import gov.nasa.pds.harvest.crawler.ProductCrawler;
import gov.nasa.pds.harvest.log.LogUtils;
import gov.nasa.pds.harvest.policy.PolicyReader;
import gov.nasa.pds.harvest.policy.model.Policy;


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
        runCrawler(cli);
    }


    private static void initLogger(HarvestCli cli)
    {
        String verbosity = cli.getOptionValue("v", "1");
        String logFile = cli.getOptionValue("l", "/tmp/harvest/harvest.log");
        
        LogUtils.setupLogger(verbosity, logFile);
    }
 
    
    private static void runCrawler(HarvestCli cli)
    {
        String configFile = cli.getOptionValue("c");
        String outDir = cli.getOptionValue("o", "/tmp/harvest/solr");

        try
        {
            File dir = new File(outDir);
            dir.mkdirs();
            
            PolicyReader rd = new PolicyReader();
            Policy policy = rd.read(new File(configFile));
            
            FileProcessor cb = new FileProcessor(dir);
            
            ProductCrawler crawler = new ProductCrawler(policy.getDirectories(), cb);
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
