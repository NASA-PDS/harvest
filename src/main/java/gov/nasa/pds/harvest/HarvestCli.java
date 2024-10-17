package gov.nasa.pds.harvest;

import java.util.jar.Attributes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.cmd.CliCommand;
import gov.nasa.pds.harvest.cmd.HarvestCmd;
import gov.nasa.pds.harvest.util.log.Log4jConfigurator;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.ArchiveStatus;
import gov.nasa.pds.registry.common.util.ExceptionUtils;
import gov.nasa.pds.registry.common.util.ManifestUtils;


/**
 * Harvest Command-Line Interface (CLI) manager / command runner.
 * 
 * @author karpenko
 */
public class HarvestCli
{
    private Options options;
    private CommandLine cmdLine;

    private CliCommand command;
    
    /**
     * Constructor
     */
    public HarvestCli()
    {
        initOptions();
    }
    

    /**
     * Parse command line arguments and run commands.
     * @param args command line arguments passed from the main() function.
     */
    public void run(String[] args)
    {
        Metadata.reportHarvestVersion(HarvestCli.class.getPackage().getImplementationVersion());
        if(args.length == 0)
        {
            printHelp();
            System.exit(1);
        }
        
        // Version
        if(args.length == 1 && ("-V".equals(args[0]) || "--version".equals(args[0])))
        {
            printVersion();
            System.exit(0);
        }        

        for (String arg : args) {
          if ("-h".equalsIgnoreCase(arg) || "-help".equalsIgnoreCase(arg) || "--help".equalsIgnoreCase(arg) || "?".equalsIgnoreCase(arg)) {
            printHelp();
          }
        }

        if(!parse(args))
        {
            System.exit(2);
        }

        if(!runCommand())
        {
            System.exit(3);
        }        
    }
    

    /**
     * Run commands based on command line parameters.
     * @return
     */
    private boolean runCommand()
    {
        try
        {
            command.run(cmdLine);
            return true;
        }
        catch(Exception ex)
        {
            String msg = ExceptionUtils.getMessage(ex);
            Logger log = LogManager.getLogger(this.getClass());
            log.error(msg);
            log.debug("", ex);
            return false;
        }
    }

    
    /**
     * Print help screen.
     */
    public static void printHelp()
    {
        System.out.println("Usage: harvest <options>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  -c <config file>   Crawl file system and process PDS4 labels");
        System.out.println("  -V, --version      Print Harvest version");
        System.out.println();
        System.out.println("Optional parameters:");
        System.out.println("  -o <dir>              Output directory (applies to supplemental products only)."); 
        System.out.println("                        Default is /tmp/harvest/out");
        System.out.println("  -l <file>             Log file. Default is /tmp/harvest/harvest.log");
        System.out.println("  -v <level>            Logger verbosity: DEBUG, INFO (default), WARNING, ERROR");
        System.out.println("  -O, --overwrite       Overwrite registered products");
        System.out.println("  -a, --archive-status  Set the archive status for all products defaulting to staged");
        for (String name : new ArchiveStatus().statusNames) {
          System.out.println("     " + name);
        }
    }

    
    /**
     * Print Harvest version
     */
    public static void printVersion()
    {
        String version = HarvestCli.class.getPackage().getImplementationVersion();
        System.out.println("Harvest version: " + version);
        Attributes attrs = ManifestUtils.getAttributes();
        if(attrs != null)
        {
            System.out.println("Build time: " + attrs.getValue("Build-Time"));
        }
    }
    
    
    /**
     * Parse command line parameters
     * @param args
     * @return
     */
    private boolean parse(String[] args)
    {
        try
        {
            CommandLineParser parser = new DefaultParser();
            this.cmdLine = parser.parse(options, args);
            
            // NOTE: !!! Init logger before creating commands !!!
            initLogger(cmdLine);

            // Crawler command
            if(cmdLine.hasOption("c"))
            {
                command = new HarvestCmd();
                return true;
            }

            System.out.println("[ERROR] Missing -c parameter");
            return false;
        }
        catch(ParseException ex)
        {
            System.out.println("[ERROR] " + ex.getMessage());
            return false;
        }
    }
    
    
    /**
     * Initialize Log4j logger
     * @param cmdLine Command line parameters
     */
    private static void initLogger(CommandLine cmdLine)
    {
        String verbosity = cmdLine.getOptionValue("v", "INFO");
        String logFile = cmdLine.getOptionValue("l");

        Log4jConfigurator.configure(verbosity, logFile);
    }

    
    /**
     * Initialize Apache Commons CLI library
     */
    private void initOptions()
    {
        options = new Options();
        Option.Builder bld;
        
        bld = Option.builder("c").hasArg().argName("file");
        options.addOption(bld.build());
        
        bld = Option.builder("o").hasArg().argName("dir");
        options.addOption(bld.build());

        bld = Option.builder("l").hasArg().argName("file");
        options.addOption(bld.build());

        bld = Option.builder("v").hasArg().argName("level");
        options.addOption(bld.build());
        
        bld = Option.builder("O").longOpt("overwrite");
        options.addOption(bld.build());
        
        bld = Option.builder("a").longOpt("archive-status").hasArg().argName("archive_status");
        options.addOption(bld.build());
    }

}
