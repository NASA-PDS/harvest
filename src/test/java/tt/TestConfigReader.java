package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;

public class TestConfigReader
{

    public static void main(String[] args) throws Exception
    {
        Configuration cfg = ConfigReader.read(new File("/tmp/harvest.xml"));
    }

}
