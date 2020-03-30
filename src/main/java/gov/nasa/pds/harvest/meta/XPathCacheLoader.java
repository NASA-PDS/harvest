package gov.nasa.pds.harvest.meta;

import java.io.File;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.harvest.cfg.model.XPathMap;
import gov.nasa.pds.harvest.cfg.model.XPathMaps;
import gov.nasa.pds.harvest.util.xml.XPathCache;
import gov.nasa.pds.harvest.util.xml.XPathUtils;
import gov.nasa.pds.harvest.util.xml.XmlDomUtils;


public class XPathCacheLoader
{
    private Logger LOG;
    private XPathFactory xpf;
    
    
    public XPathCacheLoader()
    {
        LOG = LogManager.getLogger(getClass());
        xpf = XPathFactory.newInstance();
    }
    
    
    public void load(XPathMaps maps) throws Exception
    {
        if(maps == null || maps.items == null || maps.items.isEmpty()) return;
        
        for(XPathMap xpm: maps.items)
        {
            File file = (maps.baseDir != null) ? new File(maps.baseDir, xpm.filePath) : new File(xpm.filePath); 
            LOG.info("Loading xpath-to-field-name map from " + file.getAbsolutePath());

            if(!file.exists())
            {
                throw new Exception("File " + file.getAbsolutePath() + " does not exist.");
            }
            
            loadFile(file, xpm.rootElement);
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
            String xpath = trim(node.getTextContent());
            
            if(fieldName == null || xpath == null || xpath.isEmpty()) continue;
            
            xpe = XPathUtils.compileXPath(xpf, xpath);
            cache.add(fieldName, xpe);
        }
    }
    
    
    private static String trim(String str)
    {
        return str == null ? null : str.trim();
    }
}
