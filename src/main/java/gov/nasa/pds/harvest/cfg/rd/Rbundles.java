package gov.nasa.pds.harvest.cfg.rd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.BundleCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class Rbundles
{
    public static List<BundleCfg> parseBundles(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();

        int count = xpu.getNodeCount(doc, "/harvest/bundles");
        if(count == 0) throw new Exception("Missing required element '/harvest/bundles'");
        if(count > 1) throw new Exception("Could not have more than one '/harvest/bundles' element");

        NodeList nodes = xpu.getNodeList(doc, "/harvest/bundles/bundle");
        if(nodes == null || nodes.getLength() == 0) 
        {
            throw new Exception("Provide at least one '/harvest/bundles/bundle' element");
        }

        List<BundleCfg> list = new ArrayList<>();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            BundleCfg cfg = parseBundle(node);
            
            if(cfg.dir == null || cfg.dir.isEmpty())
            {
                throw new Exception("'/harvest/bundles/bundle' is missing 'dir' attribute");
            }
            
            list.add(cfg);
        }
        
        return list;
    }

    
    private static BundleCfg parseBundle(Node bundle) throws Exception
    {
        BundleCfg cfg = new BundleCfg();
        
        NamedNodeMap atts = XmlDomUtils.getAttributes(bundle);
        for(int i = 0; i < atts.getLength(); i++)
        {
            Node attr = atts.item(i);
            String attrName = attr.getNodeName();
            
            if("dir".equals(attrName))
            {
                cfg.dir = attr.getNodeValue().trim();
            }
            else if("versions".equals(attrName))
            {
                Set<String> versions = new TreeSet<>();
                String val = attr.getNodeValue().trim();
                StringTokenizer tkz = new StringTokenizer(val, ",; \t");
                while(tkz.hasMoreTokens())
                {
                    String token = tkz.nextToken();
                    if("all".equalsIgnoreCase(token)) continue;
                    versions.add(token);
                }

                if(!versions.isEmpty())
                {
                    cfg.versions = versions;
                }
            }
            else
            {
                throw new Exception("Unknown attribute '" + attrName 
                        + "' in '/harvest/bundles/bundle' element");
            }
        }

        return cfg;
    }
}
