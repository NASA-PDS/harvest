package gov.nasa.pds.harvest.cfg.parser;

import java.util.HashSet;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.AutogenCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/autogenFields" section.
 *  
 * @author karpenko
 */
public class AutogenParser
{
    public static AutogenCfg parseAutogenFields(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        AutogenCfg cfg = new AutogenCfg();
        
        int count = xpu.getNodeCount(doc, "/harvest/autogenFields");
        if(count == 0) return cfg;
        if(count > 1) throw new Exception("Could not have more than one '/harvest/autogenFields' element.");

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
}
