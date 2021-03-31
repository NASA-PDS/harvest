package gov.nasa.pds.harvest.util.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;


public class XmlDomUtils
{
    
    public static Document readXml(DocumentBuilderFactory dbf, File file) throws Exception
    {
        DocumentBuilder db = dbf.newDocumentBuilder();
        // Don't print error messages to console.
        db.setErrorHandler(null);
        
        try
        {
            Document doc = db.parse(file);
            return doc;
        }
        catch(SAXParseException ex)
        {
            throw new Exception("Could not parse file " + file.getAbsolutePath() + ". " 
                    + ex.getMessage() + " (line = " + ex.getLineNumber() + ", column = " + ex.getColumnNumber() + ").");
        }
        catch(Exception ex)
        {
            throw new Exception("Could not parse file " + file.getAbsolutePath() + ". " + ex.getMessage());
        }
    }

    
    public static Document readXml(File file) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        return readXml(dbf, file);
    }


    public static Document readXml(File xmlFile, File xsdFile, ErrorHandler eh) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsdFile);

        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(eh);
        
        return db.parse(xmlFile);
    }


    public static String getAttribute(Node node, String attributeName)
    {
        if(node == null || node.getAttributes() == null) return null;
        
        Node att = node.getAttributes().getNamedItem(attributeName);
        return att == null ? null : att.getNodeValue().trim();
    }


    public static NamedNodeMap getAttributes(Node node)
    {
        if(node == null || node.getAttributes() == null) return null;
        return node.getAttributes();
    }
    
    
    public static int countChildNodes(Node node, String name)
    {
        int count = 0;
        
        NodeList nl = node.getChildNodes();
        
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node n = nl.item(i);
            if(n.getNodeName().equals(name)) count++;
        }
        
        return count;
    }

    
    public static Node getFirstChild(Node node, String name)
    {
        NodeList nl = node.getChildNodes();
        
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node n = nl.item(i);
            if(n.getNodeName().equals(name)) return n;
        }
        
        return null;
    }

    
    public static List<Node> getChildNodes(Node node, String name)
    {
        List<Node> list = new ArrayList<>();
        
        NodeList nl = node.getChildNodes();
        
        for(int i = 0; i < nl.getLength(); i++)
        {
            Node n = nl.item(i);
            if(n.getNodeName().equals(name)) list.add(n);
        }
        
        return list;
    }

}
