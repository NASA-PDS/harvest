package gov.nasa.pds.harvest.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.BlobStorage;
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
    private static final Logger LOG = Logger.getLogger(ConfigReader.class.getName());
    
    XPathFactory xpf = XPathFactory.newInstance();
    
    public ConfigReader()
    {
    }
    
    
    public Configuration read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        
        Configuration policy = new Configuration();
        policy.directories = parseDirectories(doc);
        policy.fileRef = parseFileRef(doc);
        policy.xpathMaps = parseXPathMaps(doc);
        policy.blobStorage = parseBlobStorage(doc);

        return policy;
    }


    private Directories parseDirectories(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();

        Directories dirs = new Directories();                
        dirs.paths = xpu.getStringList(doc, "/harvest/directories/path");
        
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
            throw new Exception("<productFilter> could not have both <include> and <exclude> at the same time");
        }
        
        return dirs;
    }

    
    private BlobStorage parseBlobStorage(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        Node rootNode = xpu.getFirstNode(doc, "/harvest/blobStorage");
        if(rootNode == null) return null;
        
        BlobStorage bs = new BlobStorage();
        String storageType = XmlDomUtils.getAttribute(rootNode, "type");
        
        if(storageType == null || storageType.equalsIgnoreCase("none"))
        {
            bs.storageType = BlobStorage.NONE;
        }
        else if(storageType.equalsIgnoreCase("embedded"))
        {
            bs.storageType = BlobStorage.EMBEDDED;
        }
        else
        {
            LOG.warning("Unknown blob storage type " + storageType);
        }
        
        return bs;
    }
    
    
    private FileRef parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        Node rootNode = xpu.getFirstNode(doc, "/harvest/fileRef");
        if(rootNode == null) return null;

        FileRef fileRef = new FileRef();
        NodeList nodes = xpu.getNodeList(rootNode, "//replace");
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

    
    private XPathMaps parseXPathMaps(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        Node rootNode = xpu.getFirstNode(doc, "/harvest/xpathMaps");
        if(rootNode == null) return null;

        XPathMaps maps = new XPathMaps();
        maps.baseDir = XmlDomUtils.getAttribute(rootNode, "baseDir");

        NodeList nodes = xpu.getNodeList(rootNode, "//xpathMap");
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
        if(filePath == null) throw new Exception("<xpathMap> element is missing filePath attribute");        
        
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
