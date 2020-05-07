package gov.nasa.pds.harvest.cfg.model;

import java.util.List;

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
