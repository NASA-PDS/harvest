package tt;

import java.io.File;

import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.util.FieldMap;


public class TestAutogenExtractor
{

    public static void main(String[] args) throws Exception
    {
        File file = new File("/tmp/d2/atlas_document.xml");
        
        AutogenExtractor ext = new AutogenExtractor();
        FieldMap fields = new FieldMap();
        ext.extract(file, fields);
        
        for(String name: fields.getNames())
        {
            System.out.println(name);
        }
    }
    
    
}
