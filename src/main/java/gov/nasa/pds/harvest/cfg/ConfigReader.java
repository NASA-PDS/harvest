package gov.nasa.pds.harvest.cfg;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.parser.BundleConfigParser;
import gov.nasa.pds.harvest.cfg.parser.AutogenParser;
import gov.nasa.pds.harvest.cfg.parser.FiltersParser;
import gov.nasa.pds.harvest.cfg.parser.RegistryConfigParser;
import gov.nasa.pds.harvest.cfg.parser.FileInfoParser;
import gov.nasa.pds.harvest.cfg.parser.XpathMapParser;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class ConfigReader
{
    private static final String ERROR = "Invalid Harvest configuration: ";
    
    private int bundlesCount = 0;
    private int dirsCount = 0;
    

    public ConfigReader()
    {
    }
    
    
    public Configuration read(File file) throws Exception
    {
        resetCounters();
        
        Document doc = XmlDomUtils.readXml(file);
        Element root = doc.getDocumentElement();
        if(!"harvest".equals(root.getNodeName()))
        {
            throw new Exception("Invalid root element '" + root.getNodeName() + "'. Expected 'harvest'.");
        }
        
        validate(root);
        
        Configuration cfg = new Configuration();
        cfg.registryCfg = RegistryConfigParser.parseRegistry(root);
        
        if(bundlesCount > 0) cfg.bundles = BundleConfigParser.parseBundles(root);
        
        cfg.filters = FiltersParser.parseFilters(doc);
        cfg.xpathMaps = XpathMapParser.parseXPathMaps(doc);
        cfg.fileInfo = FileInfoParser.parseFileInfo(doc);
        cfg.autogen = AutogenParser.parseAutogenFields(doc);

        return cfg;
    }

    
    private void resetCounters()
    {
        bundlesCount = 0;
        dirsCount = 0;
    }
    
    
    private void validate(Element root) throws Exception
    {
        NodeList nodes = root.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                String name = node.getNodeName();
                switch(name)
                {
                case "registry":
                    break;
                case "directories":
                    dirsCount++;
                    break;
                case "bundles":
                    bundlesCount++;
                    break;
                case "productFilter":
                    break;
                case "fileInfo":
                    break;
                case "autogenFields":
                    break;
                case "xpathMaps":
                    break;
                default:
                    throw new Exception(ERROR + "Invalid XML element '/harvest/" + name + "'");
                }
            }
        }
        
        if(bundlesCount > 0 && dirsCount > 0) 
            throw new Exception(ERROR + "Could not have both '/harvest/bundles' and '/harvest/directories' elements at the same time.");
        
        if(bundlesCount == 0 && dirsCount == 0) 
            throw new Exception(ERROR + "Either '/harvest/bundles' or '/harvest/directories' element is required.");                
    }
}
