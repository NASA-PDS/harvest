package gov.nasa.pds.harvest.cfg;

import java.io.File;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.rd.Rautogen;
import gov.nasa.pds.harvest.cfg.rd.Rbundles;
import gov.nasa.pds.harvest.cfg.rd.Rdirs;
import gov.nasa.pds.harvest.cfg.rd.Rfile;
import gov.nasa.pds.harvest.cfg.rd.Rrefs;
import gov.nasa.pds.harvest.cfg.rd.Rxpath;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class ConfigReader
{
    public ConfigReader()
    {
    }
    
    
    public static Configuration read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        String rootElement = doc.getDocumentElement().getNodeName();
        if(!"harvest".equals(rootElement))
        {
            throw new Exception("Invalid root element '" + rootElement + "'. Expecting 'harvest'.");
        }
        
        Configuration cfg = new Configuration();
        cfg.bundles = Rbundles.parseBundles(doc);
        cfg.filters = Rdirs.parseFilters(doc);
        cfg.xpathMaps = Rxpath.parseXPathMaps(doc);
        cfg.fileInfo = Rfile.parseFileInfo(doc);
        cfg.autogen = Rautogen.parseAutogenFields(doc);
        cfg.internalRefs = Rrefs.parseInternalRefs(doc);

        return cfg;
    }

}
