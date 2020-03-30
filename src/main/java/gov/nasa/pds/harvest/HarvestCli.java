package gov.nasa.pds.harvest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.crawler.CrawlerCommand;
import gov.nasa.pds.harvest.log.Log4jConfigurator;
import gov.nasa.pds.harvest.util.ExceptionUtils;


public class HarvestCli
{
    private Options options;
    private CommandLine cmdLine;
    
    
    public HarvestCli()
    {
        options = new Options();
        initOptions();
    }
    

    public void run(String[] args)
    {
        if(args.length == 0)
        {
            printHelp();
            System.exit(1);
        }

        if(!parse(args))
        {
            System.out.println();
            printHelp();
            System.exit(1);
        }

        initLogger();
        
        if(!runCommand())
        {
            System.exit(1);
        }        
    }
    

    private boolean runCommand()
    {
        try
        {
            CrawlerCommand cmd = new CrawlerCommand();
            cmd.run(cmdLine);
            return true;
        }
        catch(Exception ex)
        {
            Logger log = LogManager.getLogger("harvest-min-logger");
            log.error(ExceptionUtils.getMessage(ex));
            return false;
        }
    }

    
    public void printHelp()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.printHelp("harvest <options>", options);
    }

    
    public boolean parse(String[] args)
    {
        try
        {
            CommandLineParser parser = new DefaultParser();
            this.cmdLine = parser.parse(options, args);
            return true;
        }
        catch(ParseException ex)
        {
            System.out.println("ERROR: " + ex.getMessage());
            return false;
        }
    }
    
    
    private void initLogger()
    {
        String verbosity = cmdLine.getOptionValue("v", "1");
        String logFile = cmdLine.getOptionValue("l");

        Log4jConfigurator.configure(verbosity, logFile);
    }

    
    private void initOptions()
    {
        Option.Builder bld;
        
        bld = Option.builder("c").hasArg().argName("file").desc("Harvest configuration file.").required();
        options.addOption(bld.build());
        
        bld = Option.builder("o").hasArg().argName("dir")
                .desc("Output directory for Solr documents. Default is /tmp/harvest/solr");
        options.addOption(bld.build());
        
        bld = Option.builder("l").hasArg().argName("file").desc("Log file. Default is /tmp/harvest/harvest.log.");
        options.addOption(bld.build());

        bld = Option.builder("v").hasArg().argName("level").
                desc("Logger verbosity: 0=Debug, 1=Info (default), 2=Warning, 3=Error.");
        options.addOption(bld.build());

        bld = Option.builder("stopOnError").desc("Without this flag erroneous files are skipped.");
        options.addOption(bld.build());
    }

}
