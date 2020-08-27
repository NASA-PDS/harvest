package gov.nasa.pds.harvest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.crawler.CrawlerCommand;
import gov.nasa.pds.harvest.log.Log4jConfigurator;
import gov.nasa.pds.harvest.util.ExceptionUtils;
import gov.nasa.pds.harvest.util.HarvestLogManager;


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
            Logger log = HarvestLogManager.getMinInfoLogger();
            String msg = ExceptionUtils.getMessage(ex); 
            log.error(msg);
            return false;
        }
    }

    
    public void printHelp()
    {
        System.out.println("Usage: harvest <options>");
        System.out.println();
        System.out.println("Required parameters:");
        System.out.println("  -c <file>     Harvest configuration file");
        System.out.println("Optional parameters:");
        System.out.println("  -f <format>   Output format ('json' or 'xml'). Default is 'json'");
        System.out.println("  -o <dir>      Output directory. Default is /tmp/harvest/out");
        System.out.println("  -l <file>     Log file. Default is /tmp/harvest/harvest.log");
        System.out.println("  -v <level>    Logger verbosity: 0=Debug, 1=Info (default), 2=Warning, 3=Error");
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
            System.out.println("[ERROR] " + ex.getMessage());
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
        
        bld = Option.builder("c").hasArg().argName("file").required();
        options.addOption(bld.build());
        
        bld = Option.builder("o").hasArg().argName("dir");
        options.addOption(bld.build());

        bld = Option.builder("f").hasArg().argName("format");
        options.addOption(bld.build());
        
        bld = Option.builder("l").hasArg().argName("file");
        options.addOption(bld.build());

        bld = Option.builder("v").hasArg().argName("level");
        options.addOption(bld.build());
    }

}
