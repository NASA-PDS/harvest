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
 * <p>
 * Elasticsearch document writer.
 * Writes documents in "new-line-delimited JSON" format. (Content-Type: application/x-ndjson).
 * </p>
 * <p>
 * Generated file can be loaded into Elasticsearch by "_bulk" web service API: 
 * </p>
 * <pre>
 * curl -H "Content-Type: application/x-ndjson" \
 *      -XPOST "http://localhost:9200/accounts/_bulk?pretty" \
 *      --data-binary @es-docs.json
 * </pre>
 * 
 * @author karpenko
 */
public class EsDocWriter implements DocWriter
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
    public EsDocWriter(File outDir) throws Exception
    {
        this.outDir = outDir;
        
        File file = new File(outDir, "es-docs.json");        
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
            field = EsDocUtils.toEsFieldName(field);
            wr.write(field);
            wr.write("\n");
        }
        
        wr.close();        
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
    
    
    private static void write(JsonWriter jw, FieldMap fmap) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Collection<String> values = fmap.getValues(key);
            EsDocUtils.writeField(jw, key, values);
        }
    }

}
