package gov.nasa.pds.harvest.cfg;

import java.io.File;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.rd.Rdirs;
import gov.nasa.pds.harvest.cfg.rd.Rfile;
import gov.nasa.pds.harvest.cfg.rd.Rrefs;
import gov.nasa.pds.harvest.cfg.rd.Rxpath;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class ConfigReader
{
    public ConfigReader()
    {
    }
    
    
    public Configuration read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        String rootElement = doc.getDocumentElement().getNodeName();
        if(!"harvest".equals(rootElement))
        {
            throw new Exception("Invalid root element '" + rootElement + "'. Expecting 'harvest'.");
        }
        
        Configuration cfg = new Configuration();
        cfg.directories = Rdirs.parseDirectories(doc);
        cfg.fileRef = Rfile.parseFileRef(doc);
        cfg.xpathMaps = Rxpath.parseXPathMaps(doc);
        cfg.fileInfo = Rfile.parseFileInfo(doc);
        cfg.autoGenFields = parseAutoGenFields(doc);
        cfg.internalRefs = Rrefs.parseInternalRefs(doc);

        return cfg;
    }

    
    private boolean parseAutoGenFields(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/autogenFields");
        if(count == 0) return false;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/autogenFields' element.");

        return true;
    }
    
    
}
