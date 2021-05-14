package gov.nasa.pds.harvest.util.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Utility class to work with XPath API.
 * 
 * @author karpenko
 */
public class XPathUtils
{
    private XPathFactory xpf;
    
    /**
     * Constructor.
     */
    public XPathUtils()
    {
        xpf = XPathFactory.newInstance();
    }
    
    
    /**
     * Compile XPath
     * @param xpf XPath factory
     * @param str XPath string value
     * @return Compiled XPath
     * @throws Exception Generic exception
     */
    public static XPathExpression compileXPath(XPathFactory xpf, String str) throws Exception
    {
        XPath xpath = xpf.newXPath();
        XPathExpression expr = xpath.compile(str);
        return expr;
    }

    
    /**
     * Get string value by XPath.
     * @param doc XML document
     * @param expr an XPath
     * @return a string value
     * @throws Exception Generic exception
     */
    public static String getStringValue(Document doc, XPathExpression expr) throws Exception
    {
        Object res = expr.evaluate(doc, XPathConstants.STRING);
        return (res == null) ? null : res.toString();
    }

    
    /**
     * Get a list of string values by XPath.
     * @param doc XML document
     * @param expr an XPath
     * @return a list
     * @throws Exception Generic exception
     */
    public static List<String> getStringList(Document doc, XPathExpression expr) throws Exception
    {
        String[] values = getStringArray(doc, expr);
        return values == null ? null : Arrays.asList(values);
    }


    /**
     * Get a set of string values by XPath.
     * @param doc XML document
     * @param expr an XPath
     * @return a set
     * @throws Exception Generic exception
     */
    public static Set<String> getStringSet(Document doc, XPathExpression expr) throws Exception
    {
        String[] values = getStringArray(doc, expr);
        if(values == null || values.length == 0) return null;

        Set<String> set = new TreeSet<>();
        Collections.addAll(set, values);
        return set;
    }
    
    
    /**
     * Get a list of string values by XPath.
     * @param doc XML document
     * @param xpath an XPath
     * @return a list
     * @throws Exception Generic exception
     */
    public List<String> getStringList(Document doc, String xpath) throws Exception
    {
        XPathExpression expr = compileXPath(xpf, xpath);
        return getStringList(doc, expr);
    }

    
    /**
     * Get a set of string values by XPath.
     * @param doc XML document
     * @param xpath an XPath
     * @return a set
     * @throws Exception Generic exception
     */
    public Set<String> getStringSet(Document doc, String xpath) throws Exception
    {
        XPathExpression expr = compileXPath(xpf, xpath);
        List<String> list = getStringList(doc, expr);
        
        if(list == null || list.size() == 0) return null;

        Set<String> set = new HashSet<>();
        set.addAll(list);
        return set;
    }

    
    /**
     * Get an array of string values by XPath.
     * @param doc XML document
     * @param expr an XPath
     * @return an array
     * @throws Exception Generic exception
     */
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

    
    /**
     * Get node list by XPath.
     * @param item parent item
     * @param expr an XPath
     * @return Node list
     * @throws Exception Generic exception
     */
    public static NodeList getNodeList(Object item, XPathExpression expr) throws Exception
    {
        if(item == null) return null;
        
        NodeList nodes = (NodeList)expr.evaluate(item, XPathConstants.NODESET);
        return nodes;
    }
    
    
    /**
     * Get node list by XPath.
     * @param item parent item
     * @param xpath an XPath
     * @return Node list
     * @throws Exception Generic exception
     */
    public NodeList getNodeList(Object item, String xpath) throws Exception
    {
        if(item == null) return null;
        XPathExpression xpe = compileXPath(xpf, xpath);

        return getNodeList(item, xpe);
    }

    
    /**
     * Get node count by XPath.
     * @param item parent item
     * @param xpath an XPath
     * @return Node count
     * @throws Exception Generic exception
     */
    public int getNodeCount(Object item, String xpath) throws Exception
    {
        if(item == null) return 0;
        XPathExpression xpe = compileXPath(xpf, xpath);

        NodeList nodes = getNodeList(item, xpe);
        return nodes == null ? 0 : nodes.getLength();
    }
    
    
    /**
     * Get first node by XPath.
     * @param item Parent item
     * @param xpath an XPath
     * @return a Node
     * @throws Exception Generic exception
     */
    public Node getFirstNode(Object item, String xpath) throws Exception
    {
        if(item == null) return null;
        
        XPathExpression xpe = XPathUtils.compileXPath(xpf, xpath);
        NodeList nodes = XPathUtils.getNodeList(item, xpe);
        
        if(nodes == null || nodes.getLength() == 0) 
        {
            return null;
        }
        else
        {
            return nodes.item(0);
        }
    }
    
}
