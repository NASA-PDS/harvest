package gov.nasa.pds.harvest.util.xml;

import java.io.File;
import java.io.FileReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import gov.nasa.pds.registry.common.util.CloseUtils;


/**
 * Utility classes to work with streaming XML API.
 *  
 * @author karpenko
 */
public class XmlStreamUtils
{
    private XMLInputFactory factory;

    
    /**
     * Constructor
     */
    public XmlStreamUtils()
    {
        factory = XMLInputFactory.newFactory();
    }
    
    
    /**
     * Get XML's root element name.
     * @param file XML file
     * @return the XML root element
     * @throws Exception Generic exception
     */
    public String getRootElement(File file) throws Exception
    {
        XMLEventReader reader = factory.createXMLEventReader(new FileReader(file));
        
        try
        {
            while(reader.hasNext())
            {
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement())
                {
                    return event.asStartElement().getName().getLocalPart();
                }
            }

            return null;
        }
        finally
        {
            CloseUtils.close(reader);
        }
    }
}
