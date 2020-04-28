package gov.nasa.pds.harvest.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.FileInfo;
import gov.nasa.pds.harvest.cfg.model.Directories;
import gov.nasa.pds.harvest.cfg.model.FileRef;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.cfg.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.model.XPathMap;
import gov.nasa.pds.harvest.cfg.model.XPathMaps;
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
        cfg.directories = parseDirectories(doc);
        cfg.fileRef = parseFileRef(doc);
        cfg.xpathMaps = parseXPathMaps(doc);
        cfg.fileInfo = parseFileInfo(doc);
        cfg.autoGenFields = parseAutoGenFields(doc);

        return cfg;
    }


    private Directories parseDirectories(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();

        int count = xpu.getNodeCount(doc, "/harvest/directories");
        if(count == 0) throw new Exception("Missing required element '/harvest/directories'.");
        if(count > 1) throw new Exception("Could not have more than one '/harvest/directories' element.");
        
        Directories dirs = new Directories();                
        dirs.paths = xpu.getStringList(doc, "/harvest/directories/path");
        if(dirs.paths == null) throw new Exception("Provide at least one '/harvest/directories/path' element.");
        
        // File filter
        dirs.fileFilterIncludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/include");
        dirs.fileFilterExcludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/exclude");

        if(dirs.fileFilterIncludes != null && dirs.fileFilterIncludes.size() > 0 
                && dirs.fileFilterExcludes != null && dirs.fileFilterExcludes.size() > 0)
        {
            throw new Exception("<fileFilter> could not have both <include> and <exclude> at the same time");
        }

        // Dir filter
        dirs.dirFilterExcludes = xpu.getStringList(doc, "/harvest/directories/directoryFilter/exclude");
        
        // Product filter
        dirs.prodFilterIncludes = xpu.getStringSet(doc, "/harvest/directories/productFilter/include");
        dirs.prodFilterExcludes = xpu.getStringSet(doc, "/harvest/directories/productFilter/exclude");
        
        if(dirs.prodFilterIncludes != null && dirs.prodFilterIncludes.size() > 0 
                && dirs.prodFilterExcludes != null && dirs.prodFilterExcludes.size() > 0)
        {
            throw new Exception("<productFilter> could not have both <include> and <exclude> at the same time.");
        }
        
        return dirs;
    }

    
    private FileInfo parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileInfo");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileInfo' element.");

        FileInfo fileInfo = new FileInfo();
        
        Node bsNode = xpu.getFirstNode(doc, "/harvest/fileInfo/blobStorage");
        String storageType = (bsNode == null) ? null : XmlDomUtils.getAttribute(bsNode, "type");
        fileInfo.setBlobStorageType(storageType);
        
        return fileInfo;
    }
    
    
    private FileRef parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileRef");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileRef' element.");

        FileRef fileRef = new FileRef();
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
    
    
    private XPathMaps parseXPathMaps(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/xpathMaps");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/xpathMaps' element.");
        
        XPathMaps maps = new XPathMaps();
        Node rootNode = xpu.getFirstNode(doc, "/harvest/xpathMaps");
        maps.baseDir = XmlDomUtils.getAttribute(rootNode, "baseDir");

        NodeList nodes = xpu.getNodeList(doc, "/harvest/xpathMaps/xpathMap");
        if(nodes == null || nodes.getLength() == 0) return maps;
        
        List<XPathMap> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            validateXPathMap(nodes.item(i));
            
            XPathMap xpm = new XPathMap();
            xpm.filePath = XmlDomUtils.getAttribute(nodes.item(i), "filePath");
            xpm.rootElement = XmlDomUtils.getAttribute(nodes.item(i), "rootElement");
            list.add(xpm);
        }

        maps.items = list;
        return maps;
    }
    
    
    private void validateXPathMap(Node node) throws Exception
    {
        String filePath = XmlDomUtils.getAttribute(node, "filePath");
        if(filePath == null) throw new Exception("<xpathMap> element is missing 'filePath' attribute");        
        
        NamedNodeMap atts = XmlDomUtils.getAttributes(node);
        for(int i = 0; i < atts.getLength(); i++)
        {
            String attName = atts.item(i).getNodeName();
            if(!"filePath".equals(attName) && !"rootElement".equals(attName))
            {
                throw new Exception("<xpathMap> element has invalid attribute " + attName);
            }
        }
    }
}
