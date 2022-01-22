package gov.nasa.pds.harvest.meta;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.registry.common.util.FieldMap;


/**
 * Extract metadata used by keyword / full-text search
 * @author karpenko
 */
public class SearchMetadataExtractor
{
    private FieldMap fields;
    
    /**
     * Constructor
     */
    public SearchMetadataExtractor()
    {
    }

    
    /**
     * Extracts search fields from a parsed label file (XML DOM) into a FieldMap
     * @param doc Parsed PDS label file (XML DOM)
     * @param fields key-value pairs (output parameter)
     * @throws Exception an exception
     */
    public void extract(Document doc, FieldMap fields) throws Exception
    {
        this.fields = fields;
        
        Element root = doc.getDocumentElement();
        processNode(root);
        
        // Release reference
        this.fields = null;
    }

    
    private void processNode(Node node) throws Exception
    {
        boolean isLeaf = true;
        
        NodeList nl = node.getChildNodes();
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
            processLeafNode(node);
        }
    }
    
    
    private void processLeafNode(Node node) throws Exception
    {
        String name = node.getNodeName();
        
        if("description".equals(name))
        {
            String value = StringUtils.normalizeSpace(node.getTextContent());
            fields.addValue("description", value);
        }
    }

}
