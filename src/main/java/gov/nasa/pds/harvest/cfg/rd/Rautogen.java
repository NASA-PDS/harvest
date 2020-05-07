package gov.nasa.pds.harvest.cfg.rd;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.AutogenCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class Rautogen
{
    public static AutogenCfg parseAutogenFields(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();

        int count = xpu.getNodeCount(doc, "/harvest/autogenFields");
        if(count == 0) return null;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/autogenFields' element.");

        AutogenCfg cfg = new AutogenCfg();

        // Class filter
        cfg.classFilterIncludes = xpu.getStringSet(doc, "/harvest/autogenFields/classFilter/include");
        cfg.classFilterExcludes = xpu.getStringSet(doc, "/harvest/autogenFields/classFilter/exclude");

        if(cfg.classFilterIncludes != null && cfg.classFilterIncludes.size() > 0 
                && cfg.classFilterExcludes != null && cfg.classFilterExcludes.size() > 0)
        {
            throw new Exception("'/harvest/autogenFields/classFilter' "
                    + "could not have both 'include' and 'exclude' filters at the same time.");
        }

        return cfg;
    }
}
