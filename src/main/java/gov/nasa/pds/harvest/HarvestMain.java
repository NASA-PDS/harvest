package gov.nasa.pds.harvest;

import gov.nasa.pds.harvest.crawler.CrawlerCommand;
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

        CrawlerCommand cmd = new CrawlerCommand(cli);
        int status = cmd.run();
        System.exit(status);
    }


    private static void initLogger(HarvestCli cli)
    {
        String verbosity = cli.getOptionValue("v", "1");
        String logFile = cli.getOptionValue("l");
        
        LogUtils.setupLogger(verbosity, logFile);
    }

}
