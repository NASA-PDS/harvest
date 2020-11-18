package gov.nasa.pds.harvest.cfg.rd;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class Rfile
{
    public static FileInfoCfg parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileInfo");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileInfo' element.");

        FileInfoCfg fileInfo = new FileInfoCfg();

        // <fileInfo> node
        Node node = xpu.getFirstNode(doc, "/harvest/fileInfo");
        String str = XmlDomUtils.getAttribute(node, "processDataFiles");
        fileInfo.processDataFiles = (str == null || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str));
        
        // <blodStorage> node
        Node bsNode = xpu.getFirstNode(doc, "/harvest/fileInfo/blobStorage");
        String storageType = (bsNode == null) ? null : XmlDomUtils.getAttribute(bsNode, "type");
        fileInfo.setBlobStorageType(storageType);
        
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

}
