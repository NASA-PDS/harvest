package gov.nasa.pds.harvest.cfg.policy;

import java.io.File;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import gov.nasa.pds.harvest.cfg.policy.model.AccessUrl;
import gov.nasa.pds.harvest.cfg.policy.model.AccessUrls;
import gov.nasa.pds.harvest.cfg.policy.model.Directory;
import gov.nasa.pds.harvest.cfg.policy.model.DirectoryFilter;
import gov.nasa.pds.harvest.cfg.policy.model.FileFilter;
import gov.nasa.pds.harvest.cfg.policy.model.Policy;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMap;
import gov.nasa.pds.harvest.cfg.policy.model.XPathMaps;


public class PolicyReader
{
    private XStream xstream;
    
    public PolicyReader()
    {
        xstream = new XStream(new StaxDriver())
        {
            @Override
            protected void setupConverters()
            {
                registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
                registerConverter(new StringConverter(), PRIORITY_NORMAL);
                registerConverter(new IntConverter(), PRIORITY_NORMAL);
                registerConverter(new CollectionConverter(getMapper()), PRIORITY_NORMAL);
                registerConverter(new ReflectionConverter(getMapper(), getReflectionProvider()), PRIORITY_VERY_LOW);
            }
        };
        
        xstream.allowTypesByWildcard(new String[]
        {
                "gov.nasa.pds.harvest.**"
        });
        
        xstream.ignoreUnknownElements();
        
        // Root element
        xstream.alias("policy", Policy.class);

        // <directories>
        xstream.addImplicitCollection(Directory.class, "path", String.class);        
        xstream.addImplicitCollection(FileFilter.class, "include", String.class);
        xstream.addImplicitCollection(FileFilter.class, "exclude", String.class);        
        xstream.addImplicitCollection(DirectoryFilter.class, "exclude", String.class);
        
        // <accessUrls>
        xstream.addImplicitCollection(AccessUrls.class, "accessUrl", AccessUrl.class);
        xstream.addImplicitCollection(AccessUrl.class, "offset", String.class);
        
        // <xpathMaps>
        xstream.addImplicitCollection(XPathMaps.class, "xpathMaps", XPathMap.class);
        xstream.alias("xpathMap", XPathMap.class);
        xstream.useAttributeFor(XPathMap.class, "objectType");
        xstream.useAttributeFor(XPathMap.class, "filePath");
    }
    
    
    public Policy read(File file)
    {
        Policy policy = (Policy)xstream.fromXML(file);
        return policy;
    }
}
