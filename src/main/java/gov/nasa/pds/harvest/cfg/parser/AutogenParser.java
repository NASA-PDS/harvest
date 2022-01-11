package gov.nasa.pds.harvest.cfg.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gov.nasa.pds.harvest.cfg.model.AutogenCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/autogenFields" section.
 *  
 * @author karpenko
 */
public class AutogenParser
{
    /**
     * Valid &lt;autogenFields&gt; attribute names.
     */
    private static Set<String> AUTOGEN_ATTRS = new TreeSet<>();
    static
    {
        AUTOGEN_ATTRS.add("generate");
    }

    
    public static AutogenCfg parseAutogenFields(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        AutogenCfg cfg = new AutogenCfg();
        
        int count = xpu.getNodeCount(doc, "/harvest/autogenFields");
        if(count == 0) return cfg;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/autogenFields' element.");
        
        // Attributes
        Node node = xpu.getFirstNode(doc, "/harvest/autogenFields");
        validateAttributes(node, AUTOGEN_ATTRS);

        String str = XmlDomUtils.getAttribute(node, "generate");
        if(str == null)
        {
            cfg.generate = true;
        }
        else
        {
            Boolean bb = ConfigParserUtils.parseBoolean(str);
            if(bb == null)
            {
                throw new Exception("Invalid <autogenFields generate=\"" + str + "\" attribute.");
            }
            
            cfg.generate = bb;
        }

        // Class filter
        cfg.classFilterIncludes = xpu.getStringSet(doc, "/harvest/autogenFields/classFilter/include");
        cfg.classFilterExcludes = xpu.getStringSet(doc, "/harvest/autogenFields/classFilter/exclude");

        if(cfg.classFilterIncludes != null && cfg.classFilterIncludes.size() > 0 
                && cfg.classFilterExcludes != null && cfg.classFilterExcludes.size() > 0)
        {
            throw new Exception("'/harvest/autogenFields/classFilter' "
                    + "could not have both 'include' and 'exclude' filters at the same time.");
        }
        
        // Date fields
        cfg.dateFields = xpu.getStringSet(doc, "/harvest/autogenFields/dateFields/field");
        if(cfg.dateFields == null)
        {
            cfg.dateFields = new HashSet<>(0);
        }

        return cfg;
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
