package gov.nasa.pds.harvest.util.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;


public class XmlDomUtils
{
    
    public static Document readXml(DocumentBuilderFactory dbf, File file) throws Exception
    {
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        return doc;
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
        return att == null ? null : att.getNodeValue();
    }


    public static NamedNodeMap getAttributes(Node node)
    {
        if(node == null || node.getAttributes() == null) return null;
        return node.getAttributes();
    }

}
