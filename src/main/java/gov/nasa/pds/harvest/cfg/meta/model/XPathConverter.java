package gov.nasa.pds.harvest.cfg.meta.model;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


public class XPathConverter implements Converter
{

    @Override
    public boolean canConvert(Class claz)
    {
        return claz.equals(XPath.class);
    }

    
    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext ctx)
    {
    }

    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext ctx)
    {
        XPath xpath = new XPath();

        xpath.value = reader.getValue();
        if(xpath.value != null) xpath.value = xpath.value.trim();
        
        xpath.fieldName = reader.getAttribute("fieldName"); 
        
        return xpath;
    }

}
