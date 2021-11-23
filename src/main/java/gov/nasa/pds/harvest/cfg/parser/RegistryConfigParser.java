package gov.nasa.pds.harvest.cfg.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gov.nasa.pds.harvest.cfg.model.RegistryCfg;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/registry" section.
 * 
 * @author karpenko
 */
public class RegistryConfigParser
{
    public static RegistryCfg parseRegistry(Node root) throws Exception
    {
        int count = XmlDomUtils.countChildNodes(root, "registry");
        if(count == 0) throw new Exception("Missing required '/harvest/registry' element");
        if(count > 1) throw new Exception("Could not have more than one '/harvest/registry' element");

        Node registryNode = XmlDomUtils.getFirstChild(root, "registry");
        RegistryCfg cfg = new RegistryCfg();
        parseRegistryAttributes(registryNode, cfg);
        
        return cfg;
    }
    
    
    private static void parseRegistryAttributes(Node bundleNode, RegistryCfg cfg) throws Exception
    {
        NamedNodeMap atts = XmlDomUtils.getAttributes(bundleNode);
        for(int i = 0; i < atts.getLength(); i++)
        {
            Node attr = atts.item(i);
            String attrName = attr.getNodeName();
            
            if("url".equals(attrName))
            {
                cfg.url = attr.getNodeValue().trim();
            }
            else if("index".equals(attrName))
            {
                cfg.indexName = attr.getNodeValue().trim();
            }
            else if("auth".equals(attrName))
            {
                cfg.authFile = attr.getNodeValue().trim();
            }
            else
            {
                throw new Exception("Unknown attribute '" + attrName 
                        + "' in '/harvest/registry' element");
            }            
        }
    }
}
