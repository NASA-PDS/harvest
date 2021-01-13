package gov.nasa.pds.harvest.cfg.rd;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.FiltersCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;

public class Rdirs
{
    public static FiltersCfg parseFilters(Document doc) throws Exception
    {
        FiltersCfg cfg = new FiltersCfg();
        parseProductFilter(doc, cfg);
        
        // File filter
        /*
        cfg.fileFilterIncludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/include");
        cfg.fileFilterExcludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/exclude");

        if(cfg.fileFilterIncludes != null && cfg.fileFilterIncludes.size() > 0 
                && cfg.fileFilterExcludes != null && cfg.fileFilterExcludes.size() > 0)
        {
            throw new Exception("<fileFilter> could not have both <include> and <exclude> at the same time");
        }
         */
        // Dir filter
        //cfg.dirFilterExcludes = xpu.getStringList(doc, "/harvest/directories/directoryFilter/exclude");        
        
        return cfg;
    }

    
    private static void parseProductFilter(Document doc, FiltersCfg cfg) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/harvest/productFilter");
        if(count > 1) throw new Exception("Could not have more than one '/harvest/productFilter' element.");

        cfg.prodClassInclude = xpu.getStringSet(doc, "/harvest/productFilter/includeClass");
        cfg.prodClassExclude = xpu.getStringSet(doc, "/harvest/productFilter/excludeClass");
        
        if(cfg.prodClassInclude != null && cfg.prodClassInclude.size() > 0 
                && cfg.prodClassExclude != null && cfg.prodClassExclude.size() > 0)
        {
            throw new Exception("<productFilter> could not have both <include> and <exclude> at the same time.");
        }

    }

}
