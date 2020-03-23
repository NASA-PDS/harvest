package gov.nasa.pds.harvest.util.solr;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;

import gov.nasa.pds.harvest.util.FieldMap;


public class SolrDocUtils
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    
    public static void writeField(Writer writer, String key, Object[] values) throws Exception
    {
        if(values == null || values.length == 0) return;
        
        for(Object value: values)
        {
            writeField(writer, key, value);
        }
    }
    
    
    public static void writeField(Writer writer, String key, Object value) throws Exception
    {
        if(value == null) return;
        
        writer.write("  <field name=\"");
        writer.write(key);
        writer.write("\">");
        
        String strValue;
        if(value instanceof java.util.Date)
        {
            strValue = DATE_FORMAT.format(value);
        }
        else
        {
            strValue = value.toString();
        }
        
        writer.write(StringEscapeUtils.escapeXml(strValue));
        
        writer.write("</field>\n");
    }

    
    public static void writeFieldMap(Writer writer, FieldMap fields) throws Exception
    {
        writer.write("<doc>\n");
        
        for(String fieldName: fields.getNames())
        {
            Set<String> values = fields.getValues(fieldName);
            if(values != null && values.size() > 0)
            {
                for(String value: values)
                {
                    SolrDocUtils.writeField(writer, fieldName, value);
                }
            }
        }
        
        writer.write("</doc>\n");
    }        

}
