package gov.nasa.pds.harvest.cmd;

import org.apache.commons.cli.CommandLine;

/**
 * A CLI command to print Harvest version.
 * 
 * @author karpenko
 */
public class VersionCmd implements CliCommand
{
    
    /**
     * Constructor
     */
    public VersionCmd()
    {
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
        String version = this.getClass().getPackage().getImplementationVersion();
        System.out.println("Harvest version: " + version);
    }

}
