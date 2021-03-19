package gov.nasa.pds.harvest.meta;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class BundleMetadataExtractor
{
    public static class BundleMemberEntry
    {
        public String lid;
        public String lidvid;
        public boolean isPrimary = false;
        public String type;
        public String shortType;
    }
    
    
    private XPathExpression xBme;
    
    
    public BundleMetadataExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        xBme = XPathUtils.compileXPath(xpf, "/Product_Bundle/Bundle_Member_Entry");
    }
    
    
    public List<BundleMemberEntry> extractBundleMemberEntries(Document doc) throws Exception
    {
        List<BundleMemberEntry> list = new ArrayList<BundleMemberEntry>();
        
        NodeList nodes = XPathUtils.getNodeList(doc, xBme);        
        if(nodes == null || nodes.getLength() == 0) return list;

        for(int i = 0; i < nodes.getLength(); i++)
        {
            BundleMemberEntry bme = createBme(nodes.item(i));
            list.add(bme);
        }
        
        return list;
    }

    
    public void addRefs(FieldMap fmap, BundleMemberEntry bme)
    {
        if(!bme.isPrimary) return;
        
        if(bme.lidvid != null)
        {
            String key = "ref_lidvid_" + bme.shortType;
            fmap.addValue(key, bme.lidvid);
        }
        
        if(bme.lid != null)
        {
            String key = "ref_lid_" + bme.shortType;
            fmap.addValue(key, bme.lid);
        }
        
        // Convert lidvid to lid only if lid is not available
        if(bme.lidvid != null && bme.lid == null)
        {
            int idx = bme.lidvid.indexOf("::");
            if(idx > 0)
            {
                String lid = bme.lidvid.substring(0, idx);
                String key = "ref_lid_" + bme.shortType;
                fmap.addValue(key, lid);
            }
        }
    }

    
    private BundleMemberEntry createBme(Node root)
    {
        BundleMemberEntry bme = new BundleMemberEntry();
    
        NodeList nodes = root.getChildNodes();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            
            if(nodeName.equals("lid_reference"))
            {
                bme.lid = node.getTextContent().trim();
            }
            else if(nodeName.equals("lidvid_reference"))
            {
                bme.lidvid = node.getTextContent().trim();
            }
            else if(nodeName.equals("reference_type"))
            {
                bme.type = node.getTextContent().trim();
                String[] tokens = bme.type.split("_");
                bme.shortType = tokens[tokens.length-1];
            }
            else if(nodeName.equals("member_status"))
            {
                String status = node.getTextContent().trim();
                bme.isPrimary = "Primary".equalsIgnoreCase(status); 
            }
        }
        
        if(bme.shortType == null) bme.shortType = "collection"; 

        return bme;
    }

}
