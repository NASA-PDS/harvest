package gov.nasa.pds.harvest.meta;

import java.util.Set;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class CollectionMetadataExtractor
{
    private XPathExpression xFileName;
    
    
    public CollectionMetadataExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        xFileName = XPathUtils.compileXPath(xpf, "//File_Area_Inventory/File/file_name");
    }
    

    public Set<String> extractInventoryFileNames(Document doc) throws Exception
    {
        return XPathUtils.getStringSet(doc, xFileName);
    }
}
