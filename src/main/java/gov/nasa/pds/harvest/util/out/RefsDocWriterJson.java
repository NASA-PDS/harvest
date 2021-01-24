package gov.nasa.pds.harvest.util.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;

import com.google.gson.stream.JsonWriter;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class RefsDocWriterJson extends BaseRefsDocWriter
{
    public RefsDocWriterJson(File outDir) throws Exception
    {

        File file = new File(outDir, "refs-docs.json");        
        writer = new FileWriter(file);
    }
    
    
    @Override
    public void writeCollectionInventory(Metadata meta, File inventoryFile) throws Exception
    {
        BufferedReader rd = new BufferedReader(new FileReader(inventoryFile));

        int batchNum = 0;
        
        while(true)
        {
            int count = getNextBatch(rd);
            if(count == 0) break;
            
            batchNum++;
            String id = meta.lidvid + "::" + batchNum;
            
            // First line: primary key 
            NDJsonDocUtils.writePK(writer, id);
            writer.write("\n");

            // Second line: main document
            StringWriter sw = new StringWriter();
            JsonWriter jw = new JsonWriter(sw);
            
            jw.beginObject();
            NDJsonDocUtils.writeField(jw, "collection_lidvid", meta.lidvid);
            NDJsonDocUtils.writeField(jw, "collection_lid", meta.lid);            
            NDJsonDocUtils.writeField(jw, "product_lidvid", lidvidList);
            NDJsonDocUtils.writeField(jw, "product_lid", lidList);
            // Transaction ID
            NDJsonDocUtils.writeField(jw, "_package_id", PackageIdGenerator.getInstance().getPackageId());
            jw.endObject();
            
            jw.close();
            
            writer.write(sw.getBuffer().toString());
            writer.write("\n");

            if(count < BATCH_SIZE) break;
        }
        
        rd.close();
    }
    
    
    @Override
    public void close() throws Exception
    {
        writer.close();
    }
    
}
