package gov.nasa.pds.harvest.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.cfg.model.FileRefCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.rd.Rdirs;
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
        cfg.fileRef = parseFileRef(doc);
        cfg.xpathMaps = Rxpath.parseXPathMaps(doc);
        cfg.fileInfo = parseFileInfo(doc);
        cfg.autoGenFields = parseAutoGenFields(doc);
        cfg.internalRefs = Rrefs.parseInternalRefs(doc);

        return cfg;
    }

    
    private FileInfoCfg parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileInfo");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileInfo' element.");

        FileInfoCfg fileInfo = new FileInfoCfg();
        
        Node bsNode = xpu.getFirstNode(doc, "/harvest/fileInfo/blobStorage");
        String storageType = (bsNode == null) ? null : XmlDomUtils.getAttribute(bsNode, "type");
        fileInfo.setBlobStorageType(storageType);
        
        return fileInfo;
    }
    
    
    private FileRefCfg parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileRef");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileRef' element.");

        FileRefCfg fileRef = new FileRefCfg();
        NodeList nodes = xpu.getNodeList(doc, "/harvest/fileRef/replace");
        if(nodes == null || nodes.getLength() == 0) return fileRef;
        
        List<ReplaceRule> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            ReplaceRule rule = new ReplaceRule();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "prefix");
            rule.replacement = XmlDomUtils.getAttribute(nodes.item(i), "replacement");
            list.add(rule);
        }

        fileRef.rules = list;
        
        return fileRef;
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
