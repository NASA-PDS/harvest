package gov.nasa.pds.harvest.cfg.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.policy.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.policy.model.FileRef;
import gov.nasa.pds.harvest.cfg.policy.model.BlobStorage;
import gov.nasa.pds.harvest.cfg.policy.model.Directories;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMaps;
import gov.nasa.pds.harvest.util.XPathUtils;
import gov.nasa.pds.harvest.util.XmlDomUtils;


public class PolicyReader
{
    private static final Logger LOG = Logger.getLogger(PolicyReader.class.getName());
    
    XPathFactory xpf = XPathFactory.newInstance();
    
    public PolicyReader()
    {
    }
    
    
    public Policy read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        
        Policy policy = new Policy();
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
        dirs.paths = xpu.getStringList(doc, "/policy/directories/path");
        dirs.fileFilterIncludes = xpu.getStringList(doc, "/policy/directories/fileFilter/include");
        dirs.fileFilterExcludes = xpu.getStringList(doc, "/policy/directories/fileFilter/exclude");
        dirs.dirFilterExcludes = xpu.getStringList(doc, "/policy/directories/directoryFilter/exclude");

        return dirs;
    }

    
    private BlobStorage parseBlobStorage(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        Node rootNode = xpu.getFirstNode(doc, "/policy/blobStorage");
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
        
        Node rootNode = xpu.getFirstNode(doc, "/policy/fileRef");
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
        
        Node rootNode = xpu.getFirstNode(doc, "/policy/xpathMaps");
        if(rootNode == null) return null;

        XPathMaps maps = new XPathMaps();
        maps.baseDir = XmlDomUtils.getAttribute(rootNode, "baseDir");

        NodeList nodes = xpu.getNodeList(rootNode, "//xpathMap");
        if(nodes == null || nodes.getLength() == 0) return maps;
        
        List<XPathMap> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            XPathMap xpm = new XPathMap();
            xpm.filePath = XmlDomUtils.getAttribute(nodes.item(i), "filePath");
            xpm.objectType = XmlDomUtils.getAttribute(nodes.item(i), "objectType");
            list.add(xpm);
        }

        maps.items = list;
        return maps;
    }
}
