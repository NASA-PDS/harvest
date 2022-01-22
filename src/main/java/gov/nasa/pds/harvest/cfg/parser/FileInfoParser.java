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
import gov.nasa.pds.registry.common.util.xml.XPathUtils;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/fileInfo" section.
 * 
 * @author karpenko
 */
public class FileInfoParser
{
    /**
     * Valid &lt;FileInfo&gt; attribute names.
     */
    private static Set<String> FILE_INFO_ATTRS = new TreeSet<>();
    static
    {
        FILE_INFO_ATTRS.add("processDataFiles");
        FILE_INFO_ATTRS.add("storeLabels");
        FILE_INFO_ATTRS.add("storeJsonLabels");
    }

    
    /**
     * Parse &lt;fileInfo&gt; section of Harvest configuration file
     * @param doc Parsed Harvest configuration file (XMl DOM)
     * @return File info model object
     * @throws Exception an exception
     */
    public static FileInfoCfg parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        FileInfoCfg fileInfo = new FileInfoCfg();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileInfo");
        if(count == 0) return fileInfo;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileInfo' element.");

        // <fileInfo> node
        Node node = xpu.getFirstNode(doc, "/harvest/fileInfo");
        validateAttributes(node, FILE_INFO_ATTRS);
        
        // "processDataFiles" attribute
        String str = XmlDomUtils.getAttribute(node, "processDataFiles");
        if(str == null)
        {
            fileInfo.processDataFiles = true;
        }
        else
        {
            Boolean bb = ConfigParserUtils.parseBoolean(str);
            if(bb == null)
            {
                throw new Exception("Invalid <fileInfo processDataFiles=\"" + str + "\" attribute.");
            }
            
            fileInfo.processDataFiles = bb;
        }
        
        // "storeLabels" attribute
        str = XmlDomUtils.getAttribute(node, "storeLabels");
        if(str == null)
        {
            fileInfo.storeLabels = true;
        }
        else
        {
            Boolean bb = ConfigParserUtils.parseBoolean(str);
            if(bb == null)
            {
                throw new Exception("Invalid <fileInfo storeLabels=\"" + str + "\" attribute.");
            }

            fileInfo.storeLabels = bb;
        }

        // "storeJsonLabels" attribute
        str = XmlDomUtils.getAttribute(node, "storeJsonLabels");
        if(str == null)
        {
            fileInfo.storeJsonLabels = true;
        }
        else
        {
            Boolean bb = ConfigParserUtils.parseBoolean(str);
            if(bb == null)
            {
                throw new Exception("Invalid <fileInfo storeJsonLabels=\"" + str + "\" attribute.");
            }

            fileInfo.storeJsonLabels = bb;
        }
        
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
