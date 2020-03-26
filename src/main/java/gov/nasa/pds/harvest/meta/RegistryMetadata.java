package gov.nasa.pds.harvest.meta;

import gov.nasa.pds.harvest.util.FieldMap;


public class RegistryMetadata
{
    public String lid;
    public String vid;
    public String title;
    public String rootElement;
    public String prodClass;
    
    public String fileRef;    
    
    public FieldMap intRefs;
    public FieldMap customFields;

    
    public RegistryMetadata()
    {
    }
}
