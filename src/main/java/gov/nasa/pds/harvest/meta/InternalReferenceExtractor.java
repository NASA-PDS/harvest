package gov.nasa.pds.harvest.meta;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class InternalReferenceExtractor
{
//////////////////////////////////////////////////////////
    
    private static class LidRef
    {
        public String lid;
        public String type;
        
        public LidRef(String lid, String type)
        {
            this.lid = lid;
            this.type = type;
        }
    }
    
//////////////////////////////////////////////////////////
    
    private XPathExpression xIntRef;

    
    public InternalReferenceExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        xIntRef = XPathUtils.compileXPath(xpf, "//Internal_Reference");
    }
    
    
    public FieldMap extract(Document doc) throws Exception
    {
        NodeList nodes = XPathUtils.getNodeList(doc, xIntRef);        
        if(nodes == null || nodes.getLength() == 0) return null;

        FieldMap fmap = new FieldMap();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            LidRef ref = createLidRef(nodes.item(i));
            if(ref != null)
            {
                addRef(fmap, ref);
            }
        }

        return fmap;
    }

    
    private void addRef(FieldMap fmap, LidRef ref)
    {
        String[] tokens = ref.type.split("_");
        
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
        
        String key = name + "_ref";
        fmap.addValue(key, ref.lid);
    }
    
    
    private LidRef createLidRef(Node root)
    {
        String lid = null;
        String type = null;
    
        NodeList nodes = root.getChildNodes();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            
            if(nodeName.equals("lid_reference"))
            {
                lid = node.getTextContent().trim();
            }
            else if(nodeName.equals("reference_type"))
            {
                type = node.getTextContent().trim(); 
            }
            else if(nodeName.equals("lidvid_reference"))
            {
                String str = node.getTextContent().trim();
                int idx = str.indexOf("::");
                if(idx > 0)
                {
                    lid = str.substring(0, idx);
                }
            }
        }

        if(lid == null || type == null)
        {
            return null;
        }
        
        return new LidRef(lid, type);
    }
}
