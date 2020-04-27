package tt;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.xml.NsUriToPrefixMap;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;

public class TestXmlParser
{

    public static void main(String[] args) throws Exception
    {
        NsUriToPrefixMap.getInstance().add("http://pds.nasa.gov/pds4/pds/v1", "pds");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        Document doc = XmlDomUtils.readXml(dbf, new File("/tmp/d2/atlas_document.xml"));
        Element root = doc.getDocumentElement();
   
        process(root);        
    }
    
    
    private static void process(Node el)
    {
        boolean isLeaf = true;
        
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node cn = nl.item(i);
            if(cn.getNodeType() == Node.ELEMENT_NODE)
            {
                isLeaf = false;
                // Process children recursively
                process(cn);
            }
        }
        
        // This is a leaf node. Get value.
        if(isLeaf)
        {
            // PDS data dictionary class
            Node parent = el.getParentNode();
            String nsPrefix = NsUriToPrefixMap.getInstance().getPrefixByUri(parent.getNamespaceURI());
            String className = nsPrefix + "." + parent.getNodeName();

            // PDS data dictionary attribute
            nsPrefix = NsUriToPrefixMap.getInstance().getPrefixByUri(el.getNamespaceURI());
            String attrName = nsPrefix + "." + el.getNodeName();
            
            String fieldName = className + "." + attrName;
            
            String nodeValue = StringUtils.normalizeSpace(el.getTextContent());
            System.out.println(fieldName + "  -->  " + nodeValue);
        }
    }

}
