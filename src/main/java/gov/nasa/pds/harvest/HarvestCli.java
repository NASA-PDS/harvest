package gov.nasa.pds.harvest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class HarvestCli
{
    private Options options;
    private String error;
    private CommandLine cmdLine;
    
    
    public HarvestCli()
    {
        options = new Options();
        initOptions();
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
            this.error = ex.getMessage();
            return false;
        }
    }
    
    
    public String getError()
    {
        return error;
    }
    
    
    public String getOptionValue(String name)
    {
        if(cmdLine == null) return null;
        return cmdLine.getOptionValue(name);
    }
    

    public String getOptionValue(String name, String defaultValue)
    {
        if(cmdLine == null) return null;
        return cmdLine.getOptionValue(name, defaultValue);
    }

    
    private void initOptions()
    {
        Option.Builder bld;
        
        bld = Option.builder("c").hasArg().argName("file").desc("Harvest policy configuration file.").required();
        options.addOption(bld.build());
        
        bld = Option.builder("o").hasArg().argName("dir")
                .desc("Output directory for Solr documents. Default is /tmp/harvest/solr");
        options.addOption(bld.build());
        
        bld = Option.builder("l").hasArg().argName("file").desc("Log file. Default is /tmp/harvest/harvest.log.");
        options.addOption(bld.build());

        bld = Option.builder("v").hasArg().argName("level").
                desc("Logger verbosity: 0=Debug, 1=Info (default), 2=Warning, 3=Error.");
        options.addOption(bld.build());
    }

}
