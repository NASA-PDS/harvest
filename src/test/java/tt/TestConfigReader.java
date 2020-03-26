package tt;

import java.io.File;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gov.nasa.pds.harvest.cfg.ConfigReader;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.model.XPathMap;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;
import gov.nasa.pds.harvest.util.xml.XmlStreamUtils;


public class TestConfigReader
{

    public static void main(String[] args) throws Exception
    {
        try
        {
            //testValidator();
            testRead1();
            //testReadRootElement();
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    

    private static class ErrHandler implements ErrorHandler
    {

        @Override
        public void warning(SAXParseException ex) throws SAXException
        {
            System.out.println("Warning: " + ex);
        }

        @Override
        public void error(SAXParseException ex) throws SAXException
        {
            String msg = ex.getMessage();
            if(msg.startsWith("cvc-"))
            {
                int idx = msg.indexOf(": ");
                if(idx > 0) msg = msg.substring(idx+2);
            }
            
            System.out.println("Error: " 
                    + "line: " + ex.getLineNumber() 
                    + ", col: " + ex.getColumnNumber() 
                    + ", " + msg);
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException
        {
            System.out.println("Fatal error: " + ex);
        }
        
    }
    
    public static void testValidator() throws Exception
    {
        File xml = new File("/ws2/harvest/conf/t3.xml");
        File xsd = new File("/tmp/harvest/harvest.xsd");
        
        Document doc = XmlDomUtils.readXml(xml, xsd, new ErrHandler());
        
        /*
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(xsd);
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xml));
        */
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
        Configuration config = rd.read(new File("/ws2/harvest/conf/t3.xml"));
        
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
