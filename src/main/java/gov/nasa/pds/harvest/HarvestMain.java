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
        System.setProperty("javax.net.ssl.trustStore", "/home/niessner/Projects/PDS/TestData/OSV2/default.certs");
        System.setProperty("javax.net.ssl.trustStorePassword", "2Painful!");
        HarvestCli cli = new HarvestCli();
        cli.run(args);
    }
}
