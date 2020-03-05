package gov.nasa.pds.harvest.meta;

import java.io.File;
import java.util.logging.Logger;

import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMaps;


public class XPathCacheLoader
{
    private static final Logger LOG = Logger.getLogger(XPathCacheLoader.class.getName());
    
    public XPathCacheLoader()
    {
    }
    
    
    public void load(XPathMaps maps) throws Exception
    {
        if(maps == null || maps.items == null || maps.items.isEmpty()) return;
        
        for(XPathMap xpm: maps.items)
        {
            File file = (maps.baseDir != null) ? new File(maps.baseDir, xpm.filePath) : new File(xpm.filePath); 
            LOG.info("Loading xpath-to-field-name map from " + file.getAbsolutePath());

            if(!file.exists())
            {
                throw new Exception("File " + file.getAbsolutePath() + " does not exist.");
            }
            
            loadFile(file);
        }
    }
    
    
    private void loadFile(File file)
    {
        
    }
}
