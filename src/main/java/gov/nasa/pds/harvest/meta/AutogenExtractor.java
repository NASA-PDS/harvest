package gov.nasa.pds.harvest.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class AutogenExtractor
{
    private Map<String, String> globalNsMap;
    
    private Map<String, String> localNsMap;
    private FieldMap fields;

    
    public AutogenExtractor()
    {
        globalNsMap = new HashMap<>();
        globalNsMap.put("http://pds.nasa.gov/pds4/pds/v1", "pds");
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
        this.localNsMap = getDocNamespaces(doc);
        this.fields = fields;
        
        Element root = doc.getDocumentElement();
        processNode(root);
        
        // Release reference
        this.fields = null;
        this.localNsMap = null;
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
            // Data dictionary class and attribute
            String className = getNsName(el.getParentNode());
            String attrName = getNsName(el);
            String fieldName = className + "." + attrName;
            
            // Field value
            String fieldValue = StringUtils.normalizeSpace(el.getTextContent());
            
            fields.addValue(fieldName, fieldValue);
        }
    }

    
    private String getNsName(Node node) throws Exception
    {
        String nsPrefix = getNsPrefix(node);
        String nsName = nsPrefix + "." + node.getLocalName();
        
        return nsName;
    }
    
    
    private String getNsPrefix(Node node) throws Exception
    {
        String nsUri = node.getNamespaceURI();
        
        // Search gloabl map first
        String nsPrefix = globalNsMap.get(nsUri);
        if(nsPrefix != null) return nsPrefix;
        
        // Then local
        nsPrefix = localNsMap.get(nsUri);
        if(nsPrefix != null) return nsPrefix;
        
        throw new Exception("Unknown namespace: " + nsUri 
                + ". Please declare this namespace in Harvest configuration file.");
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
                name = name.substring(6);
                String uri = attr.getNodeValue();
                map.put(uri, name);
            }
        }

        return map;
    }

}
