package gov.nasa.pds.harvest.cfg.rd;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gov.nasa.pds.harvest.cfg.model.InternalRefCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;

public class Rrefs
{
    public static InternalRefCfg parseInternalRefs(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/internalRefs");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/internalRefs' element.");

        InternalRefCfg refs = new InternalRefCfg();
        
        // Root node
        Node irNode = xpu.getFirstNode(doc, "/harvest/internalRefs");
        String str = XmlDomUtils.getAttribute(irNode, "prefix");
        if(str != null)
        {
            refs.prefix = str;
        }
        
        // Lidvid
        Node lidvidNode = xpu.getFirstNode(doc, "/harvest/internalRefs/lidvid");
        if(lidvidNode != null)
        {
            str = XmlDomUtils.getAttribute(lidvidNode, "convertToLid");
            refs.lidvidConvert = ("true".equalsIgnoreCase(str));

            str = XmlDomUtils.getAttribute(lidvidNode, "keep");
            refs.lidvidKeep = ("true".equalsIgnoreCase(str));
        }

        return refs;
    }

}
