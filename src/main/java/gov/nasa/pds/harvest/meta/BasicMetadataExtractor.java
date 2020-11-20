package gov.nasa.pds.harvest.meta;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.PdsStringUtils;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class BasicMetadataExtractor
{
    private XPathExpression xLid;
    private XPathExpression xVid;
    private XPathExpression xTitle;

    private XPathExpression xFileName;
    private XPathExpression xDocFileName;
    

    public BasicMetadataExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        
        xLid = XPathUtils.compileXPath(xpf, "//Identification_Area/logical_identifier");
        xVid = XPathUtils.compileXPath(xpf, "//Identification_Area/version_id");
        xTitle = XPathUtils.compileXPath(xpf, "//Identification_Area/title");
        
        xFileName = XPathUtils.compileXPath(xpf, "//File/file_name");
        xDocFileName = XPathUtils.compileXPath(xpf, "//Document_File/file_name");
    }

    
    public Metadata extract(Document doc) throws Exception
    {
        Metadata md = new Metadata();        
        
        // Basic info
        md.lid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xLid));
        md.vid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xVid));
        md.title = StringUtils.normalizeSpace(XPathUtils.getStringValue(doc, xTitle));
        md.prodClass = doc.getDocumentElement().getNodeName();
        
        // Files
        if(md.prodClass.equals("Product_Document"))
        {
            md.dataFiles = XPathUtils.getStringSet(doc, xDocFileName);
        }
        else
        {
            md.dataFiles = XPathUtils.getStringSet(doc, xFileName);
        }
        
        return md;
    }
    
}
