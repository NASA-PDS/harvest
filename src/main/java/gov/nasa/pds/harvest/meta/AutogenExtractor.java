package gov.nasa.pds.harvest.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class AutogenExtractor
{
    private Map<String, String> nsMap;
    
    private FieldMap fields;

    
    public AutogenExtractor()
    {
        nsMap = new HashMap<>();
        nsMap.put("http://pds.nasa.gov/pds4/pds/v1", "pds");
    }

    
    public void extract(File file, FieldMap fields) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = XmlDomUtils.readXml(dbf, file);
        extract(doc, fields);
    }
    
    
    public void extract(Document doc, FieldMap fields) throws Exception
    {
        this.fields = fields;
        
        Element root = doc.getDocumentElement();
        processNode(root);
        
        // Release reference
        this.fields = null;
    }


    private void processNode(Node el) throws Exception
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
                processNode(cn);
            }
        }
        
        // This is a leaf node. Get value.
        if(isLeaf)
        {
            // PDS data dictionary class
            Node parent = el.getParentNode();
            String nsPrefix = getNsPrefix(parent);
            String className = nsPrefix + "." + parent.getNodeName();

            // PDS data dictionary attribute
            nsPrefix = getNsPrefix(el);
            String attrName = nsPrefix + "." + el.getNodeName();
            
            // Field name
            String fieldName = className + "." + attrName;
            
            // Field value
            String fieldValue = StringUtils.normalizeSpace(el.getTextContent());
            
            fields.addValue(fieldName, fieldValue);
        }
    }

    
    private String getNsPrefix(Node node) throws Exception
    {
        String nsUri = node.getNamespaceURI();
        String nsPrefix = nsMap.get(nsUri);
        if(nsPrefix == null)
        {
            throw new Exception("Unknown namespace: " + nsUri 
                    + ". Please declare this namespace in Harvest configuration file.");
        }

        return nsPrefix;
    }
}
