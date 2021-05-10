package gov.nasa.pds.harvest.util.out;

import java.io.Writer;
import java.util.Collection;
import org.apache.commons.lang.StringEscapeUtils;
import gov.nasa.pds.harvest.util.FieldMapSet;


/**
 * Utility class to write XML documents in Solr format.
 *  
 * @author karpenko
 */
public class XmlDocUtils
{
    /**
     * Write a field.
     * @param writer
     * @param key
     * @param values
     * @throws Exception
     */
    public static void writeField(Writer writer, String key, Collection<String> values) throws Exception
    {
        if(values == null || values.size() == 0) return;
        
        for(String value: values)
        {
            writeField(writer, key, value);
        }
    }
    
    
    /**
     * Write a field.
     * @param writer
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(Writer writer, String key, float value) throws Exception
    {
        writeField(writer, key, String.valueOf(value));
    }
    

    /**
     * Write a field.
     * @param writer
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(Writer writer, String key, int value) throws Exception
    {
        writeField(writer, key, String.valueOf(value));
    }

    
    /**
     * Write a field.
     * @param writer
     * @param key
     * @param value
     * @throws Exception
     */
    public static void writeField(Writer writer, String key, String value) throws Exception
    {
        if(value == null) return;
        
        writer.write("  <field name=\"");
        writer.write(key);
        writer.write("\">");
        
        StringEscapeUtils.escapeXml(writer, value);
        
        writer.write("</field>\n");
    }

    
    /**
     * Write a multivalued field.
     * @param writer
     * @param fields
     * @throws Exception
     */
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
                    XmlDocUtils.writeField(writer, fieldName, value);
                }
            }
        }
        
        writer.write("</doc>\n");
    }        

}
