package gov.nasa.pds.harvest;

/**
 * Main function / entry point.
 * Delegate all processing to HarvestCli object. 
 * 
 * @author karpenko
 */
public class HarvestMain
{
    public static void main(String[] args)
    {
        System.setProperty("sun.stdout.encoding", "UTF-8");
        HarvestCli cli = new HarvestCli();
        cli.run(args);
    }
}
