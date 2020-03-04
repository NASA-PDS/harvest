package gov.nasa.pds.harvest.cfg.meta;

import java.io.File;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import gov.nasa.pds.harvest.cfg.meta.model.ProductMetadata;
import gov.nasa.pds.harvest.cfg.meta.model.XPath;
import gov.nasa.pds.harvest.cfg.meta.model.XPathConverter;


public class ProductMetadataReader
{
    private XStream xstream;
    
    public ProductMetadataReader()
    {
        // NOTE: Must use DomDriver!!! XPathConverter doesn't work with StaxDriver()!!!
        xstream = new XStream(new DomDriver())
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
        xstream.alias("productMetadata", ProductMetadata.class);
        xstream.addImplicitCollection(ProductMetadata.class, "xpath", XPath.class);
        
        // <xpath>
        xstream.alias("xpath", XPath.class);
        xstream.registerConverter(new XPathConverter());
    }

    
    public ProductMetadata read(File file)
    {
        ProductMetadata meta = (ProductMetadata)xstream.fromXML(file);
        return meta;
    }

    
    public void test(ProductMetadata meta)
    {
        System.out.println(xstream.toXML(meta));
    }
}
