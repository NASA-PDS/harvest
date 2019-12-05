package gov.nasa.pds.harvest.search.registry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.search.util.XPathUtils;
import gov.nasa.pds.harvest.search.util.XmlDomUtils;


public class MetadataExtractor
{
    private XPathExpression xLid;
    private XPathExpression xVid;    
    
    private ContextExtractor ctxExtractor;
    private DefaultExtractor defaultExtractor;
    
    private DocumentBuilderFactory dbf;
    

    private static class ContextExtractor extends BaseExtractor
    {
        public ContextExtractor(XPathFactory xpf) throws Exception
        {
            xInvestigation = XPathUtils.compileXPath(xpf, "/Product_Context/Investigation/name");
            xInstrument = XPathUtils.compileXPath(xpf, "/Product_Context/Instrument/name");
            xInstrumentHost = XPathUtils.compileXPath(xpf, "/Product_Context/Instrument_Host/name");
            xTarget = XPathUtils.compileXPath(xpf, "/Product_Context/Target/name");
        }
    }
    
    
    private static final class DefaultExtractor extends BaseExtractor
    {
        public DefaultExtractor(XPathFactory xpf) throws Exception
        {
            xInvestigation = XPathUtils.compileXPath(xpf, "//Investigation_Area/name");
            xInstrument = XPathUtils.compileXPath(xpf, "//Observing_System/Observing_System_Component[type='Instrument']/name");
            xInstrumentHost = XPathUtils.compileXPath(xpf, "//Observing_System/Observing_System_Component[type='Spacecraft']/name");
            xTarget = XPathUtils.compileXPath(xpf, "//Target_Identification/name");
        }
    }
    
    
    public MetadataExtractor() throws Exception
    {
        dbf = DocumentBuilderFactory.newInstance();
        
        XPathFactory xpf = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI, 
                "com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl", null);

        xLid = XPathUtils.compileXPath(xpf, "//Identification_Area/logical_identifier");
        xVid = XPathUtils.compileXPath(xpf, "//Identification_Area/version_id");

        ctxExtractor = new ContextExtractor(xpf);
        defaultExtractor = new DefaultExtractor(xpf);
    }
    
    
    public RegistryMetadata extract(String path) throws Exception
    {
        Document doc = XmlDomUtils.readXml(dbf, path);        
        
        RegistryMetadata md = new RegistryMetadata();        
        md.productClass = doc.getDocumentElement().getNodeName();
        
        // IDs
        md.lid = XPathUtils.getStringValue(doc, xLid);
        md.vid = XPathUtils.getStringValue(doc, xVid);
        
        BaseExtractor ext = defaultExtractor;
        
        if("Product_Context".equals(md.productClass))
        {
            ext = ctxExtractor;
        }
        
        md.investigation = ext.getInvestigation(doc);
        md.instrument = ext.getInstrument(doc);
        md.instrumentHost = ext.getInstrumentHost(doc);
        md.target = ext.getTarget(doc);
        
        return md;
    }

}
