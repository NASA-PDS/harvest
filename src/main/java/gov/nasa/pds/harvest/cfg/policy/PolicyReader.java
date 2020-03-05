package gov.nasa.pds.harvest.cfg.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.policy.model.ReplaceRule;
import gov.nasa.pds.harvest.cfg.policy.model.Directories;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMaps;
import gov.nasa.pds.harvest.util.XPathUtils;
import gov.nasa.pds.harvest.util.XmlDomUtils;


public class PolicyReader
{
    XPathFactory xpf = XPathFactory.newInstance();
    
    public PolicyReader()
    {
    }
    
    
    public Policy read(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        
        Policy policy = new Policy();
        policy.directories = parseDirectories(doc);
        policy.accessUrlRules = parseAccessUrlRules(doc);
        policy.xpathMaps = parseXPathMaps(doc);

        return policy;
    }


    private Directories parseDirectories(Document doc) throws Exception
    {
        Directories dirs = new Directories();

        XPathExpression expr = XPathUtils.compileXPath(xpf, "/policy/directories/path");
        dirs.paths = XPathUtils.getStringList(doc, expr);
        
        expr = XPathUtils.compileXPath(xpf, "/policy/directories/fileFilter/include");
        dirs.fileFilterIncludes = XPathUtils.getStringList(doc, expr);
        
        expr = XPathUtils.compileXPath(xpf, "/policy/directories/fileFilter/exclude");
        dirs.fileFilterExcludes = XPathUtils.getStringList(doc, expr);

        expr = XPathUtils.compileXPath(xpf, "/policy/directories/directoryFilter/exclude");
        dirs.dirFilterExcludes = XPathUtils.getStringList(doc, expr);

        return dirs;
    }

    
    private List<ReplaceRule> parseAccessUrlRules(Document doc) throws Exception
    {
        XPathExpression xpe = XPathUtils.compileXPath(xpf, "/policy/accessUrl/replaceFilePath");
        NodeList nodes = XPathUtils.getNodeList(doc, xpe);        
        if(nodes == null || nodes.getLength() == 0) return null;
        
        List<ReplaceRule> list = new ArrayList<>();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            ReplaceRule rule = new ReplaceRule();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "prefix");
            rule.replacement = XmlDomUtils.getAttribute(nodes.item(i), "replacement");
            list.add(rule);
        }

        return list;        
    }

    
    private XPathMaps parseXPathMaps(Document doc) throws Exception
    {
        XPathExpression xpe = XPathUtils.compileXPath(xpf, "/policy/xpathMaps");
        NodeList nodes = XPathUtils.getNodeList(doc, xpe);
        if(nodes == null || nodes.getLength() == 0) return null;

        XPathMaps maps = new XPathMaps();
        Node rootNode = nodes.item(0);
        maps.baseDir = XmlDomUtils.getAttribute(rootNode, "baseDir");

        // <xpathMap> items
        xpe = XPathUtils.compileXPath(xpf, "//xpathMap");
        nodes = XPathUtils.getNodeList(rootNode, xpe);
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
