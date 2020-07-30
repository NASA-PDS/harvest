package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Set;

import com.google.gson.stream.JsonWriter;

import gov.nasa.pds.harvest.meta.FileData;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.DocWriter;
import gov.nasa.pds.harvest.util.FieldMapSet;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class EsDocWriter implements DocWriter
{
    private FileWriter writer;
    
    public EsDocWriter(File outDir) throws Exception
    {
        File file = new File(outDir, "es-docs.json");        
        writer = new FileWriter(file);
    }

    
    @Override
    public void write(FileData fileData, Metadata meta) throws Exception
    {
        // First line: primary key 
        String lidvid = meta.lid + "::" + meta.vid;
        writePK(lidvid);
        newLine();
        
        // Second line: main document

        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();

        // Basic info
        EsDocUtils.writeField(jw, "lid", meta.lid);
        EsDocUtils.writeField(jw, "vid", meta.vid);
        EsDocUtils.writeField(jw, "lidvid", lidvid);
        EsDocUtils.writeField(jw, "title", meta.title);
        EsDocUtils.writeField(jw, "product_class", meta.prodClass);
        
        // File Info
        EsDocUtils.writeField(jw, "_file_ref", meta.fileRef);

        if(fileData != null)
        {
            EsDocUtils.writeField(jw, "_file_name", fileData.name);
            EsDocUtils.writeField(jw, "_file_type", fileData.mimeType);
            EsDocUtils.writeField(jw, "_file_size", fileData.size);

            // File BLOB
            EsDocUtils.writeField(jw, "_file_blob", fileData.blobBase64);
            EsDocUtils.writeField(jw, "_file_md5", fileData.md5Base64);
        }

        // Transaction ID
        EsDocUtils.writeField(jw, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        // References
        write(jw, meta.intRefs);
        
        // Other Fields
        write(jw, meta.fields);
        
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
        newLine();
    }

    
    @Override
    public void close() throws Exception
    {
        writer.close();
    }
    
    
    private void newLine() throws Exception
    {
        writer.write("\n");
    }

    
    private void writePK(String id) throws Exception
    {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();
        
        jw.name("index");
        jw.beginObject();
        jw.name("_id").value(id);
        jw.endObject();
        
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
    }
    
    
    private static void write(JsonWriter jw, FieldMapSet fmap) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Set<String> values = fmap.getValues(key);
            EsDocUtils.writeField(jw, key, values);
        }
    }

}
