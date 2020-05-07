package gov.nasa.pds.harvest.cfg.rd;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.cfg.model.FileRefCfg;
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
        
        Node bsNode = xpu.getFirstNode(doc, "/harvest/fileInfo/blobStorage");
        String storageType = (bsNode == null) ? null : XmlDomUtils.getAttribute(bsNode, "type");
        fileInfo.setBlobStorageType(storageType);
        
        return fileInfo;
    }
    
    
    public static FileRefCfg parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/fileRef");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/fileRef' element.");

        FileRefCfg fileRef = new FileRefCfg();
        NodeList nodes = xpu.getNodeList(doc, "/harvest/fileRef/replace");
        if(nodes == null || nodes.getLength() == 0) return fileRef;
        
        List<FileRefCfg.ReplaceRule> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            FileRefCfg.ReplaceRule rule = new FileRefCfg.ReplaceRule();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "prefix");
            rule.replacement = XmlDomUtils.getAttribute(nodes.item(i), "replacement");
            list.add(rule);
        }

        fileRef.rules = list;
        
        return fileRef;
    }

}
