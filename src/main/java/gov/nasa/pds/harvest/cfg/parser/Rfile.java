package gov.nasa.pds.harvest.cfg.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class Rfile
{
    private static Set<String> FILE_INFO_ATTRS = new TreeSet<>();
    static
    {
        FILE_INFO_ATTRS.add("processDataFiles");
        FILE_INFO_ATTRS.add("storeLabels");
    }

    
    public static FileInfoCfg parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileInfo");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileInfo' element.");

        FileInfoCfg fileInfo = new FileInfoCfg();

        // <fileInfo> node
        Node node = xpu.getFirstNode(doc, "/harvest/fileInfo");
        validateAttributes(node, FILE_INFO_ATTRS);
        
        String str = XmlDomUtils.getAttribute(node, "processDataFiles");
        fileInfo.processDataFiles = (str == null || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str));
        
        str = XmlDomUtils.getAttribute(node, "storeLabels");
        fileInfo.storeLabels = ("true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str));        
        
        // <fileRef> nodes
        fileInfo.fileRef = parseFileRef(doc);
        
        return fileInfo;
    }
    
    
    public static List<FileInfoCfg.FileRefCfg> parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        NodeList nodes = xpu.getNodeList(doc, "/harvest/fileInfo/fileRef");
        if(nodes == null || nodes.getLength() == 0) return null;
        
        List<FileInfoCfg.FileRefCfg> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            FileInfoCfg.FileRefCfg rule = new FileInfoCfg.FileRefCfg();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "replacePrefix");
            rule.replacement = XmlDomUtils.getAttribute(nodes.item(i), "with");
            
            if(rule.prefix == null) throw new Exception("'/harvest/fileInfo/fileRef' missing 'replacePrefix' attribute");
            if(rule.replacement == null) throw new Exception("'/harvest/fileInfo/fileRef' missing 'with' attribute");
            
            list.add(rule);
        }

        return list;
    }

    
    private static void validateAttributes(Node node, Set<String> values) throws Exception
    {
        NamedNodeMap atts = XmlDomUtils.getAttributes(node);
        for(int i = 0; i < atts.getLength(); i++)
        {
            String attName = atts.item(i).getNodeName();
            if(!values.contains(attName))
            {
                throw new Exception("<" + node.getNodeName() + "> element has invalid attribute '" + attName + "'");
            }
        }
    }

}
