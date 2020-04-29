package gov.nasa.pds.harvest.cfg.model;

public class XPathMap
{
    public static final int TYPE_STRING = 0;
    public static final int TYPE_DATE = 1;
    
    public String rootElement;
    public String filePath;
    public int dataType;
    
    public XPathMap()
    {
        this.dataType = TYPE_STRING;
    }
}
