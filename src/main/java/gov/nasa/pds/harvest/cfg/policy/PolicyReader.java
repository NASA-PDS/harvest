package gov.nasa.pds.harvest.cfg.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.policy.model.AccessUrlRule;
import gov.nasa.pds.harvest.cfg.policy.model.Directories;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;
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

    
    private List<AccessUrlRule> parseAccessUrlRules(Document doc) throws Exception
    {
        XPathExpression xpe = XPathUtils.compileXPath(xpf, "/policy/accessUrl/replace");
        NodeList nodes = XPathUtils.getNodeList(doc, xpe);        
        if(nodes == null || nodes.getLength() == 0) return null;
        
        List<AccessUrlRule> list = new ArrayList<>();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            AccessUrlRule rule = new AccessUrlRule();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "filePathPrefix");
            rule.baseUrl = XmlDomUtils.getAttribute(nodes.item(i), "withBaseUrl");
            list.add(rule);
        }

        return list;        
    }

    
    private List<XPathMap> parseXPathMaps(Document doc) throws Exception
    {
        XPathExpression xpe = XPathUtils.compileXPath(xpf, "/policy/xpathMaps/xpathMap");
        NodeList nodes = XPathUtils.getNodeList(doc, xpe);        
        if(nodes == null || nodes.getLength() == 0) return null;
        
        List<XPathMap> list = new ArrayList<>();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            XPathMap xpm = new XPathMap();
            xpm.filePath = XmlDomUtils.getAttribute(nodes.item(i), "filePath");
            xpm.objectType = XmlDomUtils.getAttribute(nodes.item(i), "objectType");
            list.add(xpm);
        }

        return list;        
    }
}
