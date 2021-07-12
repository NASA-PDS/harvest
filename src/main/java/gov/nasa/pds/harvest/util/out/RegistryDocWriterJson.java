package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.stream.JsonWriter;

import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


/**
 * Write metadata extracted from PDS4 labels to an NJSON (new-line-delimited JSON)
 * file. This file can be loaded into Elasticsearch with Registry Manager tool. 
 * 
 * @author karpenko
 */
public class RegistryDocWriterJson implements RegistryDocWriter
{
    private File outDir;
    private FileWriter writer;    
    
    private Set<String> allFields = new TreeSet<>();
    
    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception Generic exception
     */
    public RegistryDocWriterJson(File outDir) throws Exception
    {
        this.outDir = outDir;
        
        File file = new File(outDir, "registry-docs.json");        
        writer = new FileWriter(file);
    }

    
    @Override
    public void write(Metadata meta) throws Exception
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
        write(jw, meta.intRefs);
        
        // Other Fields
        write(jw, meta.fields);
        
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
        writer.write("\n");
    }

    
    @Override
    public void close() throws IOException
    {
        writer.close();
        
        // Save field list
        saveFields();
    }
    
    
    private void saveFields() throws IOException
    {
        File file = new File(outDir, "fields.txt");
        FileWriter wr = new FileWriter(file);
        
        for(String field: allFields)
        {
            field = NDJsonDocUtils.toEsFieldName(field);
            wr.write(field);
            wr.write("\n");
        }
        
        wr.close();        
    }
    
    
    private void write(JsonWriter jw, FieldMap fmap) throws Exception
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

            allFields.add(key);
            NDJsonDocUtils.writeField(jw, key, values);
        }
    }

}
