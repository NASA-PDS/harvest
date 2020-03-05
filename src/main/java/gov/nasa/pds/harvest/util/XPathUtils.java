package gov.nasa.pds.harvest.util;

import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class XPathUtils
{

    public static XPathExpression compileXPath(XPathFactory xpf, String str) throws Exception
    {
        XPath xpath = xpf.newXPath();
        XPathExpression expr = xpath.compile(str);
        return expr;
    }

    
    public static String getStringValue(Document doc, XPathExpression expr) throws Exception
    {
        Object res = expr.evaluate(doc, XPathConstants.STRING);
        return (res == null) ? null : res.toString();
    }

    
    public static List<String> getStringList(Document doc, XPathExpression expr) throws Exception
    {
        String[] values = getStringArray(doc, expr);
        return values == null ? null : Arrays.asList(values);
    }
    
    
    public static String[] getStringArray(Document doc, XPathExpression expr) throws Exception
    {
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        if (nodes == null || nodes.getLength() == 0)
            return null;

        String vals[] = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++)
        {
            vals[i] = nodes.item(i).getTextContent();
        }

        return vals;
    }

    
    public static NodeList getNodeList(Object item, XPathExpression expr) throws Exception
    {
        NodeList nodes = (NodeList)expr.evaluate(item, XPathConstants.NODESET);
        return nodes;
    }
}
