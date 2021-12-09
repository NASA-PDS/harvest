package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import com.google.gson.stream.JsonWriter;

import gov.nasa.pds.harvest.meta.FieldNameCache;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;
import gov.nasa.pds.harvest.util.xml.XmlNamespaces;


/**
 * Write metadata extracted from PDS4 labels to an NJSON (new-line-delimited JSON)
 * file. This file can be loaded into Elasticsearch with Registry Manager tool. 
 * 
 * @author karpenko
 */
public class RegistryDocWriterJson extends BaseRegistryDocWriter
{
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception Generic exception
     */
    public RegistryDocWriterJson(File outDir) throws Exception
    {
        super(outDir);
        
        File file = new File(outDir, "registry-docs.json");        
        writer = new FileWriter(file);
    }

    
    @Override
    public void write(Metadata meta, XmlNamespaces nsInfo) throws Exception
    {
        // First line: primary key 
        String lidvid = meta.lid + "::" + meta.vid;
        NDJsonDocUtils.writePK(writer, lidvid);
        writer.write("\n");
        
        // Second line: main document

        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();

        // Basic info
        NDJsonDocUtils.writeField(jw, "lid", meta.lid);
        NDJsonDocUtils.writeField(jw, "vid", meta.strVid);
        NDJsonDocUtils.writeField(jw, "lidvid", lidvid);
        NDJsonDocUtils.writeField(jw, "title", meta.title);
        NDJsonDocUtils.writeField(jw, "product_class", meta.prodClass);

        // Transaction ID
        NDJsonDocUtils.writeField(jw, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        // References
        write(jw, meta.intRefs, nsInfo);
        
        // Other Fields
        write(jw, meta.fields, nsInfo);
        
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
        writer.write("\n");
    }

    
    @Override
    public void close() throws IOException
    {
        writer.close();
        
        // Save missing fields and XSDs
        saveMissingFields();
        saveMissingXsds();
    }
    
    
    private void write(JsonWriter jw, FieldMap fmap, XmlNamespaces xmlns) throws Exception
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

            NDJsonDocUtils.writeField(jw, key, values);
            
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
