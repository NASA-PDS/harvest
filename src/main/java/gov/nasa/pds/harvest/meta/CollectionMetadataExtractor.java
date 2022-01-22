package gov.nasa.pds.harvest.meta;

import java.util.Set;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import gov.nasa.pds.registry.common.util.xml.XPathUtils;


/**
 * Extracts collection metadata
 * @author karpenko
 */
public class CollectionMetadataExtractor
{
    private XPathExpression xFileName;
    

    /**
     * Constructor
     * @throws Exception
     */
    public CollectionMetadataExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        xFileName = XPathUtils.compileXPath(xpf, "//File_Area_Inventory/File/file_name");
    }
    

    /**
     * Extract collection inventory file names
     * @param doc Parsed collection label (XML DOM)
     * @return a set of files (usually there is only one inventory file)
     * @throws Exception an exception
     */
    public Set<String> extractInventoryFileNames(Document doc) throws Exception
    {
        return XPathUtils.getStringSet(doc, xFileName);
    }
}
