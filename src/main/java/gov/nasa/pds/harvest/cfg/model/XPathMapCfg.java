package gov.nasa.pds.harvest.cfg.model;

import java.util.List;


/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class XPathMapCfg
{
    public static class Item
    {
        public String rootElement;
        public String filePath;
    }
    
    public String baseDir;
    public List<Item> items;
}
