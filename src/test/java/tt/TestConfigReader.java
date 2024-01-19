package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;

public class TestConfigReader
{

    public static void main(String[] args) throws Exception
    {
        ConfigManager cfgReader = new ConfigManager();
        Configuration cfg = cfgReader.read(new File("/tmp/harvest.xml"));

        printBundles(cfg);
        printDirs(cfg);

        System.out.println();
        System.out.println("Refs: primary only: " + cfg.refsCfg.primaryOnly);
    }
    
    
    private static void printBundles(Configuration cfg)
    {
        if(cfg.bundles == null) return;

        for(BundleCfg bundle: cfg.bundles)
        {
            System.out.println("Bundle dir: " + bundle.dir);
            if(bundle.collectionLids != null) System.out.println("    Collections: " + bundle.collectionLids);
            if(bundle.productDirs != null) System.out.println("    Prod dirs: " + bundle.productDirs);
            System.out.println();
        }
    }

    
    private static void printDirs(Configuration cfg)
    {
        if(cfg.dirs == null) return;

        for(String dir: cfg.dirs)
        {
            System.out.println("Directory: " + dir);
        }
    }
}
