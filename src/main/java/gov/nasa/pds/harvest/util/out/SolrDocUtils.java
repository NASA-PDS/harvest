package gov.nasa.pds.harvest.util.out;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.TimeZone;
import org.apache.commons.lang.StringEscapeUtils;
import gov.nasa.pds.harvest.util.FieldMapSet;


public class SolrDocUtils
{
    private static final SimpleDateFormat DATE_FORMAT;
    static
    {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    
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
            strValue = DATE_FORMAT.format(value) + "Z";
        }
        else
        {
            strValue = value.toString();
        }
        
        StringEscapeUtils.escapeXml(writer, strValue);
        
        writer.write("</field>\n");
    }

    
    public static void writeFieldMap(Writer writer, FieldMapSet fields) throws Exception
    {
        writer.write("<doc>\n");
        
        for(String fieldName: fields.getNames())
        {
            Collection<String> values = fields.getValues(fieldName);
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
