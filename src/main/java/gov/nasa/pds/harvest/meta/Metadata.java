package gov.nasa.pds.harvest.meta;

import java.util.Set;

import gov.nasa.pds.harvest.util.FieldMapList;
import gov.nasa.pds.harvest.util.FieldMapSet;


public class Metadata
{
    public String lid;
    public String strVid;
    public float vid;
    public String lidvid;

    public String title;
    public String prodClass;
    
    public FieldMapSet intRefs;
    public FieldMapList fields;
    
    public Set<String> dataFiles;

    
    public Metadata()
    {
        intRefs = new FieldMapSet();
        fields = new FieldMapList();
    }
}
