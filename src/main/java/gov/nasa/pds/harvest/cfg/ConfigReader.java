package gov.nasa.pds.harvest.cfg;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.parser.BundleConfigParser;
import gov.nasa.pds.harvest.cfg.parser.Rautogen;
import gov.nasa.pds.harvest.cfg.parser.Rdirs;
import gov.nasa.pds.harvest.cfg.parser.Rfile;
import gov.nasa.pds.harvest.cfg.parser.Rxpath;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class ConfigReader
{
    public ConfigReader()
    {
    }
    
    
    public static Configuration read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        Element root = doc.getDocumentElement();
        if(!"harvest".equals(root.getNodeName()))
        {
            throw new Exception("Invalid root element '" + root.getNodeName() + "'. Expected 'harvest'.");
        }
        
        Configuration cfg = new Configuration();
        cfg.bundles = BundleConfigParser.parseBundles(root);
        cfg.filters = Rdirs.parseFilters(doc);
        cfg.xpathMaps = Rxpath.parseXPathMaps(doc);
        cfg.fileInfo = Rfile.parseFileInfo(doc);
        cfg.autogen = Rautogen.parseAutogenFields(doc);

        return cfg;
    }

}
