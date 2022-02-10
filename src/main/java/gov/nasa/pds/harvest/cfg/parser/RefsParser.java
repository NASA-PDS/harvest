package gov.nasa.pds.harvest.cfg.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import gov.nasa.pds.harvest.cfg.model.RefsCfg;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/references" section.
 * 
 * @author karpenko
 */
public class RefsParser
{
    public static RefsCfg parseReferences(Node root) throws Exception
    {
        RefsCfg cfg = new RefsCfg();
        
        int count = XmlDomUtils.countChildNodes(root, "references");
        if(count == 0) return cfg;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/references' element");

        Node refsNode = XmlDomUtils.getFirstChild(root, "references");
        parseRefsAttributes(refsNode, cfg);
        
        return cfg;
    }


    private static void parseRefsAttributes(Node bundleNode, RefsCfg cfg) throws Exception
    {
        NamedNodeMap atts = XmlDomUtils.getAttributes(bundleNode);
        for(int i = 0; i < atts.getLength(); i++)
        {
            Node attr = atts.item(i);
            String attrName = attr.getNodeName();
            
            if("primaryOnly".equals(attrName))
            {
                String val = attr.getNodeValue().trim();
                if(!"true".equalsIgnoreCase(val) && !"false".equalsIgnoreCase(val)) 
                {
                    throw new Exception("'/harvest/references@primaryOnly' has invalid value '" + val 
                            + "'. Expected 'true' or 'false'.");
                }
                
                cfg.primaryOnly = "true".equalsIgnoreCase(val);
            }
            else
            {
                throw new Exception("Unknown attribute '" + attrName 
                        + "' in '/harvest/references' element");
            }            
        }
    }
}
