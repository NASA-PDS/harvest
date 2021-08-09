package tt.es;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.json.JSONObject;
import org.json.XML;

import gov.nasa.pds.harvest.meta.FileMetadataExtractor;

public class TestXml2Json
{
    public static void main(String[] args) throws Exception
    {
        test1();
    }
    
    public static void test2() throws Exception
    {
        File file = new File("/tmp/d1/1294638283.xml");
        String blob = FileMetadataExtractor.getJsonBlob(file);                
        System.out.println(blob);
        
        EmbeddedBlobExporter.export(blob, "/tmp/blob.json");
    }
    
    
    public static void test1() throws Exception
    {
        Reader xmlSource = new FileReader("/tmp/d1/1294638283.xml");
        JSONObject json = XML.toJSONObject(xmlSource);
        String str = json.toString();
        System.out.println(str);
    }

}
