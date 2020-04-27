package gov.nasa.pds.harvest.meta;

import gov.nasa.pds.harvest.util.FieldMap;


public class Metadata
{
    public String lid;
    public String vid;
    public String title;
    public String rootElement;
    public String prodClass;
    
    public String fileRef;        
    
    public FieldMap intRefs;
    public FieldMap fields;

    
    public Metadata()
    {
        fields = new FieldMap();
    }
}
