package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
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
    private FileWriter writer;    
    
    private boolean writeFields = true;
    private Set<String> allFields = new TreeSet<>();
    
    private File outDir;
    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception
     */
    public RegistryDocWriterJson(File outDir) throws Exception
    {
        this.outDir = outDir;
        
        File file = new File(outDir, "registry-docs.json");        
        writer = new FileWriter(file);
    }

    
    public void setWriteFields(boolean b)
    {
        this.writeFields = b;
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
        NDJsonDocUtils.writeField(jw, "vid", meta.vid);
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
        
        // Build a list of all fields in all documents
        if(writeFields)
        {
            addFields(meta);
        }
    }

    
    private void addFields(Metadata meta)
    {
        if(meta == null) return;
        
        if(meta.intRefs != null && meta.intRefs.size() > 0)
        {
            allFields.addAll(meta.intRefs.getNames());
        }

        if(meta.fields != null && meta.fields.size() > 0)
        {
            allFields.addAll(meta.fields.getNames());
        }
    }
    
    
    @Override
    public void close() throws Exception
    {
        writer.close();

        if(writeFields)
        {
            saveFields();
        }
    }
    
    
    private void saveFields() throws Exception
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
    
    
    private static void write(JsonWriter jw, FieldMap fmap) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Collection<String> values = fmap.getValues(key);
            NDJsonDocUtils.writeField(jw, key, values);
        }
    }

}
