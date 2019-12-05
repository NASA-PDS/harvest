package gov.nasa.pds.harvest.search.registry;

import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;

import gov.nasa.pds.harvest.search.util.XPathUtils;

public class BaseExtractor
{
    protected XPathExpression xInvestigation;
    protected XPathExpression xInstrument;
    protected XPathExpression xInstrumentHost;
    protected XPathExpression xTarget;
    
    public String getInvestigation(Document doc) throws Exception
    {
        String str = XPathUtils.getStringValue(doc, xInvestigation);
        return (str == null || str.isEmpty()) ? null : str;
    }
    
    public String[] getInstrument(Document doc) throws Exception
    {
        return XPathUtils.getStringArray(doc, xInstrument);
    }

    public String[] getInstrumentHost(Document doc) throws Exception
    {
        return XPathUtils.getStringArray(doc, xInstrumentHost);
    }
    
    public String[] getTarget(Document doc) throws Exception
    {
        return XPathUtils.getStringArray(doc, xTarget);
    }

}
