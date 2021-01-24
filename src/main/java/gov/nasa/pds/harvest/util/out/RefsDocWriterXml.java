package gov.nasa.pds.harvest.util.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class RefsDocWriterXml extends BaseRefsDocWriter
{

    public RefsDocWriterXml(File outDir) throws Exception
    {
        super();
        
        File file = new File(outDir, "refs-docs.xml");        
        writer = new FileWriter(file);
        
        writer.append("<add>\n");
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
            
            writer.append("<doc>\n");
            XmlDocUtils.writeField(writer, "_id", id);
            XmlDocUtils.writeField(writer, "collection_lidvid", meta.lidvid);
            XmlDocUtils.writeField(writer, "collection_lid", meta.lid);
            XmlDocUtils.writeField(writer, "product_lidvid", lidvidList);
            XmlDocUtils.writeField(writer, "product_lid", lidList);
            // Transaction ID
            XmlDocUtils.writeField(writer, "_package_id", PackageIdGenerator.getInstance().getPackageId());
            writer.append("</doc>\n");

            if(count < BATCH_SIZE) break;
        }
        
        rd.close();
    }


    @Override
    public void close() throws Exception
    {
        writer.append("</add>\n");
        writer.close();
    }

}
