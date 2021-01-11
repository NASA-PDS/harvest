package gov.nasa.pds.harvest.cfg.rd;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.cfg.model.DirectoriesCfg;
import gov.nasa.pds.harvest.util.xml.XPathUtils;

public class Rdirs
{
    public static DirectoriesCfg parseDirectories(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();

        int count = xpu.getNodeCount(doc, "/harvest/directories");
        if(count == 0) throw new Exception("Missing required element '/harvest/directories'.");
        if(count > 1) throw new Exception("Could not have more than one '/harvest/directories' element.");
        
        DirectoriesCfg dirs = new DirectoriesCfg();                
        dirs.paths = xpu.getStringList(doc, "/harvest/directories/bundle");
        if(dirs.paths == null) throw new Exception("Provide at least one '/harvest/directories/bundle' element.");
        
        // File filter
        dirs.fileFilterIncludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/include");
        dirs.fileFilterExcludes = xpu.getStringList(doc, "/harvest/directories/fileFilter/exclude");

        if(dirs.fileFilterIncludes != null && dirs.fileFilterIncludes.size() > 0 
                && dirs.fileFilterExcludes != null && dirs.fileFilterExcludes.size() > 0)
        {
            throw new Exception("<fileFilter> could not have both <include> and <exclude> at the same time");
        }

        // Dir filter
        dirs.dirFilterExcludes = xpu.getStringList(doc, "/harvest/directories/directoryFilter/exclude");
        
        // Product filter
        dirs.prodFilterIncludes = xpu.getStringSet(doc, "/harvest/directories/productFilter/include");
        dirs.prodFilterExcludes = xpu.getStringSet(doc, "/harvest/directories/productFilter/exclude");
        
        if(dirs.prodFilterIncludes != null && dirs.prodFilterIncludes.size() > 0 
                && dirs.prodFilterExcludes != null && dirs.prodFilterExcludes.size() > 0)
        {
            throw new Exception("<productFilter> could not have both <include> and <exclude> at the same time.");
        }
        
        return dirs;
    }

}
