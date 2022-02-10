package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.registry.common.meta.FieldNameCache;
import gov.nasa.pds.registry.common.meta.Metadata;
import gov.nasa.pds.registry.common.util.FieldMap;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;


/**
 * Write metadata extracted from PDS4 labels to an XML file.
 * This file can be loaded into Solr by Solr post tool. 
 *  
 * @author karpenko
 */
public class RegistryDocWriterXml extends BaseRegistryDocWriter
{
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception Generic exception
     */
    public RegistryDocWriterXml(File outDir) throws Exception
    {
        super(outDir);
        
        File file = new File(outDir, "registry-docs.xml");        
        writer = new FileWriter(file);
        
        writer.append("<add>\n");
    }

    
    @Override
    public void close() throws IOException
    {
        writer.append("</add>\n");
        writer.close();
        
        // Save missing fields and XSDs
        saveMissingFields();
        saveMissingXsds();
    }

    
    @Override
    public void write(Metadata meta, XmlNamespaces nsInfo) throws Exception
    {
        writer.append("<doc>\n");

        // Basic info
        String lidvid = meta.lid + "::" + meta.vid;
        XmlDocUtils.writeField(writer, "lid", meta.lid);
        XmlDocUtils.writeField(writer, "vid", meta.strVid);
        XmlDocUtils.writeField(writer, "lidvid", lidvid);
        XmlDocUtils.writeField(writer, "title", meta.title);
        XmlDocUtils.writeField(writer, "product_class", meta.prodClass);
        
        // Transaction ID
        XmlDocUtils.writeField(writer, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        // References
        write(meta.intRefs, nsInfo);
        
        // Other Fields
        write(meta.fields, nsInfo);
        
        writer.append("</doc>\n");
    }
 
    
    private void write(FieldMap fmap, XmlNamespaces xmlns) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Collection<String> values = fmap.getValues(key);
            
            // Skip empty single value fields
            if(values.size() == 1 && values.iterator().next().isEmpty())
            {
                continue;
            }

            for(String value: values)
            {
                XmlDocUtils.writeField(writer, key, value);
            }
            
            // Check if current Elasticsearch schema has this field.
            if(!FieldNameCache.getInstance().containsName(key))
            {
                // Update missing fields and XSDs
                missingFields.add(key);
                updateMissingXsds(key, xmlns);
            }
        }
    }

}
