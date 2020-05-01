package tt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gov.nasa.pds.harvest.cfg.model.AutogenCfg;
import gov.nasa.pds.harvest.meta.AutogenExtractor;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class TestAutogenExtractor
{

    public static void main(String[] args) throws Exception
    {
        printDocNamespaces();
    }
    
    
    public static void printDocNamespaces() throws Exception
    {
        File file = new File("/tmp/d3/UVS_CAL_0016o_0000.XML");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = XmlDomUtils.readXml(dbf, file);

        Map<String, String> map = getDocNamespaces(doc);
        for(String key: map.keySet())
        {
            System.out.println(key + "  -->  " + map.get(key));
        }
    }

    
    private static Map<String, String> getDocNamespaces(Document doc)
    {
        Element root = doc.getDocumentElement();
        NamedNodeMap attrs = root.getAttributes();

        Map<String, String> map = new HashMap<>();
        
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if(name.startsWith("xmlns:"))
            {
                String uri = attr.getNodeValue();
                map.put(uri, name);
            }
        }

        return map;
    }
    
    
    public static void testAutogen() throws Exception
    {
        File file = new File("/tmp/d2/atlas_document.xml");
        
        AutogenCfg cfg = new AutogenCfg();
        AutogenExtractor ext = new AutogenExtractor(cfg);
        
        FieldMap fields = new FieldMap();
        ext.extract(file, fields);
        
        for(String name: fields.getNames())
        {
            System.out.println(name);
        }
    }
    
    
}
