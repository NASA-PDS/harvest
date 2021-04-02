package gov.nasa.pds.harvest.meta;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.util.PdsStringUtils;
import gov.nasa.pds.harvest.util.xml.XPathUtils;


public class BasicMetadataExtractor
{
    private XPathExpression xLid;
    private XPathExpression xVid;
    private XPathExpression xTitle;

    private XPathExpression xFileName;
    private XPathExpression xDocFile;
    

    public BasicMetadataExtractor() throws Exception
    {
        XPathFactory xpf = XPathFactory.newInstance();
        
        xLid = XPathUtils.compileXPath(xpf, "//Identification_Area/logical_identifier");
        xVid = XPathUtils.compileXPath(xpf, "//Identification_Area/version_id");
        xTitle = XPathUtils.compileXPath(xpf, "//Identification_Area/title");
        
        xFileName = XPathUtils.compileXPath(xpf, "//File/file_name");
        xDocFile = XPathUtils.compileXPath(xpf, "//Document_File");
    }

    
    public Metadata extract(File file, Document doc) throws Exception
    {
        Metadata md = new Metadata();        
        md.prodClass = doc.getDocumentElement().getNodeName();
        
        // LID/VID
        md.lid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xLid));
        if(md.lid == null || md.lid.isEmpty())
        {
            throw new Exception("Missing logical identifier: " + file);
        }

        md.strVid = PdsStringUtils.trim(XPathUtils.getStringValue(doc, xVid));
        if(md.strVid == null || md.strVid.isEmpty())
        {
            throw new Exception("Missing '//Identification_Area/version_id'");
        }

        try
        {
            md.vid = Float.parseFloat(md.strVid);
        }
        catch(Exception ex)
        {
            throw new Exception("Invalid '//Identification_Area/version_id': " +  md.strVid
                    + ". Expecting M.m number, such as '1.0' or '2.5'.");
        }
        
        md.lidvid = md.lid + "::" + md.strVid;
        
        // Title
        md.title = StringUtils.normalizeSpace(XPathUtils.getStringValue(doc, xTitle));

        // Files
        if(md.prodClass.equals("Product_Document"))
        {
            md.dataFiles = extractDocumentFilePaths(doc);
        }
        else
        {
            md.dataFiles = XPathUtils.getStringSet(doc, xFileName);
        }
        
        return md;
    }
    
    
    private Set<String> extractDocumentFilePaths(Document doc) throws Exception
    {
        NodeList nodes = XPathUtils.getNodeList(doc, xDocFile);
        if(nodes == null || nodes.getLength() == 0) return null;
        
        Set<String> files = new TreeSet<>();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            String filePath = extractFilePath(nodes.item(i));
            if(filePath != null) files.add(filePath);
        }
        
        return files;
    }
    
    
    private String extractFilePath(Node root)
    {
        String fileName = null;
        String dir = null;
        
        NodeList nodes = root.getChildNodes();
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            
            if(nodeName.equals("file_name"))
            {
                fileName = node.getTextContent().trim();
            }
            else if(nodeName.equals("directory_path_name"))
            {
                dir = node.getTextContent().trim();
            }
        }

        if(fileName == null) return null;

        if(dir == null) return fileName;
        
        return (dir.endsWith("/")) ? dir + fileName : dir + "/" + fileName;
    }
}
