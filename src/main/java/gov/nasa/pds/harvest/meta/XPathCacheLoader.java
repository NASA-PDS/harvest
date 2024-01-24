package gov.nasa.pds.harvest.meta;

import java.io.File;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import gov.nasa.pds.harvest.cfg.XpathMapType;
import gov.nasa.pds.harvest.cfg.XpathMapsType;
import gov.nasa.pds.harvest.util.xml.XPathCache;
import gov.nasa.pds.registry.common.util.xml.XPathUtils;
import gov.nasa.pds.registry.common.util.xml.XmlDomUtils;


/**
 * Loads XPaths from a configuration file into XPath cache. 
 * @author karpenko
 */
public class XPathCacheLoader
{
    private Logger LOG;
    private XPathFactory xpf;
    
    /**
     * Constructor
     */
    public XPathCacheLoader()
    {
        LOG = LogManager.getLogger(getClass());
        xpf = XPathFactory.newInstance();
    }
    

    /**
     * Load XPaths from a configuration file
     * @param maps XPath configuration (parsed configuration file) 
     * @throws Exception
     */
    public void load(XpathMapsType maps) throws Exception
    {        
        for(XpathMapType xpm: maps.getXpathMap())
        {
            File file = new File(maps.getBaseDir(), xpm.getFilePath()); 
            LOG.info("Loading xpath-to-field-name map from " + file.getAbsolutePath());

            if(!file.exists())
            {
                throw new Exception("File " + file.getAbsolutePath() + " does not exist.");
            }
            
            loadFile(file, xpm.getRootElement());
        }
    }
    
    
    private void loadFile(File file, String objectType) throws Exception
    {
        Document doc = XmlDomUtils.readXml(file);
        
        XPathExpression xpe = XPathUtils.compileXPath(xpf, "//xpath");
        NodeList nodes = XPathUtils.getNodeList(doc, xpe);
        if(nodes == null || nodes.getLength() == 0) return;
        
        XPathCache cache = XPathCacheManager.getInstance().getOrCreate(objectType);
        
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            String fieldName = XmlDomUtils.getAttribute(node, "fieldName");
            String dataType = XmlDomUtils.getAttribute(node, "dataType");
            String xpath = trim(node.getTextContent());
            
            if(fieldName == null || xpath == null || xpath.isEmpty()) continue;
            
            xpe = XPathUtils.compileXPath(xpf, xpath);
            cache.add(fieldName, dataType, xpe);
        }
    }
    
    
    private static String trim(String str)
    {
        return str == null ? null : str.trim();
    }
}
