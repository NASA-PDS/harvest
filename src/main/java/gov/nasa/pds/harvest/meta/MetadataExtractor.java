package gov.nasa.pds.harvest.meta;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.PdsStringUtils;
import gov.nasa.pds.harvest.util.xml.XPathCache;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class MetadataExtractor
{
    private DocumentBuilderFactory dbf;
    
    private XPathExpression xLid;
    private XPathExpression xVid;
    private XPathExpression xTitle;
    private XPathExpression xProdClass;
    
    private InternalReferenceExtractor refExtractor;
    
    
    public MetadataExtractor() throws Exception
    {
        dbf = DocumentBuilderFactory.newInstance();
        XPathFactory xpf = XPathFactory.newInstance();
        
        xLid = XPathUtils.compileXPath(xpf, "//Identification_Area/logical_identifier");
        xVid = XPathUtils.compileXPath(xpf, "//Identification_Area/version_id");
        xTitle = XPathUtils.compileXPath(xpf, "//Identification_Area/title");
        xProdClass = XPathUtils.compileXPath(xpf, "//Identification_Area/product_class");
    
        refExtractor = new InternalReferenceExtractor();
    }

    
    public RegistryMetadata extract(File file) throws Exception
    {
        Document doc = XmlDomUtils.readXml(dbf, file);
        
        RegistryMetadata md = new RegistryMetadata();        
        md.rootElement = doc.getDocumentElement().getNodeName();
        
        // Basic info
        md.lid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xLid));
        md.vid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xVid));
        md.title = StringUtils.normalizeSpace(XPathUtils.getStringValue(doc, xTitle));
        md.prodClass = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xProdClass));

        // References
        md.intRefs = refExtractor.extract(doc);
        
        // Custom fields
        FieldMap customFields = new FieldMap();
        
        // Common fields
        XPathCache cache = XPathCacheManager.getInstance().getCommonCache();
        addCustomFields(doc, cache, customFields);
        
        // Object type fields
        cache = XPathCacheManager.getInstance().getCacheByObjectType(md.rootElement);
        addCustomFields(doc, cache, customFields);
        
        if(!customFields.isEmpty()) md.customFields = customFields; 
        
        return md;
    }

    
    private void addCustomFields(Document doc, XPathCache cache, FieldMap fieldMap) throws Exception
    {
        if(cache == null || cache.isEmpty()) return;
        
        for(XPathCache.Item item: cache.getItems())
        {
            String[] values = XPathUtils.getStringArray(doc, item.xpe);
            fieldMap.addValues(item.fieldName, values);
        }
    }
    
}
