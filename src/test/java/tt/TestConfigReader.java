package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;

public class TestConfigReader
{

    public static void main(String[] args) throws Exception
    {
        Configuration cfg = ConfigReader.read(new File("/tmp/harvest.xml"));
        
        for(BundleCfg bundle: cfg.bundles)
        {
            System.out.println("Collections: " + bundle.collectionLids);
            System.out.println("Prod dirs: " + bundle.productDirs);
            System.out.println();
        }
    }

}
