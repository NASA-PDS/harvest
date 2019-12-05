package gov.nasa.pds.harvest.search.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XmlDomUtils
{
    public static Document readXml(DocumentBuilderFactory dbf, String filePath) throws Exception
    {
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(filePath));
        return doc;
    }

    public static Document readXml(String filePath) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        return readXml(dbf, filePath);
    }

}
