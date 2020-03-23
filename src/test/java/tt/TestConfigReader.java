package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.model.XPathMap;
import gov.nasa.pds.harvest.util.xml.XmlStreamUtils;


public class TestConfigReader
{

    public static void main(String[] args) throws Exception
    {
        testRead1();
        //testReadRootElement();
    }
    

    public static void testReadRootElement() throws Exception
    {
        XmlStreamUtils utils = new XmlStreamUtils();
        String name = utils.getRootElement(new File("/tmp/d3/atlas_document.xml"));
        System.out.println(name);
    }
    
    
    public static void testRead1() throws Exception
    {
        ConfigReader rd = new ConfigReader();
        Configuration config = rd.read(new File("/ws2/harvest/conf/t1.xml"));
        
        System.out.println("\nDirectories\n===============");
        System.out.println(config.directories.paths);
        System.out.println(config.directories.fileFilterIncludes);
        System.out.println(config.directories.fileFilterExcludes);
        System.out.println(config.directories.dirFilterExcludes);

        System.out.println("\nProduct Filter\n===============");
        System.out.println(config.directories.prodFilterIncludes);
        System.out.println(config.directories.prodFilterExcludes);
        
        System.out.println("\nFileRef\n===============");
        if(config.fileRef != null && config.fileRef.rules != null)
        {
            for(ReplaceRule rule: config.fileRef.rules)
            {
                System.out.println(rule.prefix + " --> " + rule.replacement);
            }
        }
        
        System.out.println("\nXPathMaps\n===============");
        if(config.xpathMaps != null)
        {
            System.out.println("baseDir = " + config.xpathMaps.baseDir);
            
            for(XPathMap map: config.xpathMaps.items)
            {
                System.out.println(map.rootElement + " --> " + map.filePath);
            }
        }
        
        System.out.println("\nBlobStorage\n===============");
        System.out.println("type = " + ((config.blobStorage == null) ? 0 : config.blobStorage.storageType));
        
    }

    
}
