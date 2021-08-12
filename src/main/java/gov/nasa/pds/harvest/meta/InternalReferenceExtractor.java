package gov.nasa.pds.harvest.meta;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.FieldMapSet;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


/**
 * Extracts internal references
 * @author karpenko
 */
public class InternalReferenceExtractor
{
//////////////////////////////////////////////////////////
    
    private static class LidRef
    {
        public String lid;
        public String lidvid;
        public String type;
    }
    
//////////////////////////////////////////////////////////

    private XPathExpression xIntRef;
    
    
    /**
     * Constructor
     * @throws Exception an exception
     */
    public InternalReferenceExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        xIntRef = XPathUtils.compileXPath(xpf, "//Internal_Reference");
    }
    

    /**
     * Adds internal references to a field map
     * @param fmap multi-valued field map
     * @param doc parsed PDS label (XML DOM) 
     * @throws Exception an exception
     */
    public void addRefs(FieldMapSet fmap, Document doc) throws Exception
    {
        NodeList nodes = XPathUtils.getNodeList(doc, xIntRef);        
        if(nodes == null || nodes.getLength() == 0) return;

        for(int i = 0; i < nodes.getLength(); i++)
        {
            LidRef ref = createLidRef(nodes.item(i));
            if(ref != null)
            {
                addRef(fmap, ref);
            }
        }
    }

    
    private void addRef(FieldMapSet fmap, LidRef ref)
    {
        if(ref.type == null) return;
        String type = getShortRefType(ref.type);
        
        if(ref.lid != null)
        {
            String key = "ref_lid_" + type;
            fmap.addValue(key, ref.lid);
        }

        if(ref.lidvid != null)
        {
            String key = "ref_lidvid_" + type;
            fmap.addValue(key, ref.lidvid);
        }
    }
    
    
    private String getShortRefType(String refType)
    {
        String[] tokens = refType.split("_");
        
        String name = tokens[tokens.length-1];
        
        if(tokens.length > 1) 
        { 
            if(name.equals("host") && tokens[tokens.length-2].equals("instrument"))
            {
                name = "instrument_host";
            }
            else if(name.equals("kernel") && tokens[tokens.length-2].equals("spice"))
            {
                name = "spice_kernel";
            }            
        }
        
        return name;
    }
    
    
    private LidRef createLidRef(Node root)
    {
        LidRef ref = new LidRef();
    
        NodeList nodes = root.getChildNodes();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            
            if(nodeName.equals("lid_reference"))
            {
                ref.lid = node.getTextContent().trim();
            }
            else if(nodeName.equals("reference_type"))
            {
                ref.type = node.getTextContent().trim(); 
            }
            else if(nodeName.equals("lidvid_reference"))
            {
                ref.lidvid = node.getTextContent().trim();
            }
        }

        // Convert lidvid to lid
        if(ref.lidvid != null && ref.lid == null)
        {
            int idx = ref.lidvid.indexOf("::");
            if(idx > 0)
            {
                ref.lid = ref.lidvid.substring(0, idx);
            }
        }
        
        return ref;
    }
}
