package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import com.google.gson.stream.JsonWriter;

import gov.nasa.pds.harvest.crawler.ProdRefsBatch;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.LidVidUtils;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class RefsDocWriterJson implements RefsDocWriter
{
    private Writer writer;

    
    public RefsDocWriterJson(File outDir) throws Exception
    {

        File file = new File(outDir, "refs-docs.json");        
        writer = new FileWriter(file);
    }
    
    
    @Override
    public void writeBatch(Metadata meta, ProdRefsBatch.WriterBatch batch) throws Exception
    {
        String id = meta.lidvid + "::" + batch.batchNum;
        
        // First line: primary key 
        NDJsonDocUtils.writePK(writer, id);
        writer.write("\n");

        // Second line: main document
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();
        // Collection ids
        NDJsonDocUtils.writeField(jw, "collection_lidvid", meta.lidvid);
        NDJsonDocUtils.writeField(jw, "collection_lid", meta.lid);            
        
        // LidVid refs
        NDJsonDocUtils.writeField(jw, "product_lidvid", batch.lidvids);
        // Convert lidvids to lids
        List<String> lids = LidVidUtils.lidvidToLid(batch.lidvids);
        NDJsonDocUtils.writeField(jw, "product_lid", lids);
        
        // Lid refs
        NDJsonDocUtils.writeField(jw, "product_lid", batch.lids);
        
        // Transaction ID
        NDJsonDocUtils.writeField(jw, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        jw.endObject();
        
        jw.close();
        
        writer.write(sw.getBuffer().toString());
        writer.write("\n");
    }
    
    
    @Override
    public void close() throws Exception
    {
        writer.close();
    }
    
}
