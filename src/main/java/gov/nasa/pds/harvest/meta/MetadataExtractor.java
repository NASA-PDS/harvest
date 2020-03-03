package gov.nasa.pds.harvest.meta;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.XPathUtils;
import gov.nasa.pds.harvest.util.XmlDomUtils;


public class MetadataExtractor
{
    private DocumentBuilderFactory dbf;
    
    private XPathExpression xLid;
    private XPathExpression xVid;
    private XPathExpression xTitle;
    
    private InternalReferenceExtractor refExtractor;
    
    
    public MetadataExtractor() throws Exception
    {
        dbf = DocumentBuilderFactory.newInstance();
        XPathFactory xpf = XPathFactory.newInstance();
        
        xLid = XPathUtils.compileXPath(xpf, "//Identification_Area/logical_identifier");
        xVid = XPathUtils.compileXPath(xpf, "//Identification_Area/version_id");
        xTitle = XPathUtils.compileXPath(xpf, "//Identification_Area/title");
    
        refExtractor = new InternalReferenceExtractor();
    }

    
    public RegistryMetadata extract(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(dbf, file);
        
        RegistryMetadata md = new RegistryMetadata();        
        md.rootElement = doc.getDocumentElement().getNodeName();
        
        // Basic info
        md.lid = XPathUtils.getStringValue(doc, xLid);
        md.vid = XPathUtils.getStringValue(doc, xVid);
        md.title = StringUtils.normalizeSpace(XPathUtils.getStringValue(doc, xTitle));
        
        // References
        md.intRefs = refExtractor.extract(doc);
        
        return md;
    }
    
    
}
