package gov.nasa.pds.harvest.crawler;

/**
 * Product reference type. Primary or secondary.
 * @author karpenko
 */
public enum RefType 
{ 
    PRIMARY("P", "primary"), 
    SECONDARY("S", "secondary");
    
    private final String id;
    private final String label;
    
    
    RefType(String id, String label)
    {
        this.id = id;
        this.label = label;
    }
    
    
    public String getId()
    {
        return id;
    }
    

    public String getLabel()
    {
        return label;
    }

}
