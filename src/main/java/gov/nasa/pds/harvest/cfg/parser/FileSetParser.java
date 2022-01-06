package gov.nasa.pds.harvest.cfg.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/files" section.
 * 
 * @author karpenko
 */
public class FileSetParser
{
    public static List<String> parseFiles(Node root) throws Exception
    {
        int count = XmlDomUtils.countChildNodes(root, "files");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/files' element");

        Node dirsNode = XmlDomUtils.getFirstChild(root, "files");
        return parseManifests(dirsNode);
    }
    
    
    private static List<String> parseManifests(Node root) throws Exception
    {
        List<String> list = new ArrayList<>();
        
        NodeList nl = root.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node node = nl.item(i);
            if(node.getNodeType() != Node.ELEMENT_NODE) continue;
            
            if(node.getNodeName().equals("manifest"))
            {
                String val = node.getTextContent().trim();
                if(!val.isBlank()) list.add(val);
            }
            else
            {
                throw new Exception("Invalid element '/harvest/files/" + node.getNodeName() + "'");
            }
        }
        
        if(list.isEmpty())
        {
            throw new Exception("At least one '/harvest/files/manifest' element is required.");
        }
        
        return list;
    }
}
