package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import gov.nasa.pds.harvest.crawler.ProdRefsBatch;
import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.LidVidUtils;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


public class RefsDocWriterXml implements RefsDocWriter
{
    private Writer writer;

    
    public RefsDocWriterXml(File outDir) throws Exception
    {
        super();
        
        File file = new File(outDir, "refs-docs.xml");        
        writer = new FileWriter(file);
        
        writer.append("<add>\n");
    }


    @Override
    public void writeBatch(Metadata meta, ProdRefsBatch batch) throws Exception
    {
        String id = meta.lidvid + "::" + batch.batchNum;
        
        writer.append("<doc>\n");
        
        // Collection ids
        XmlDocUtils.writeField(writer, "_id", id);
        XmlDocUtils.writeField(writer, "collection_lidvid", meta.lidvid);
        XmlDocUtils.writeField(writer, "collection_lid", meta.lid);
        
        // LidVid refs
        XmlDocUtils.writeField(writer, "product_lidvid", batch.getLidVids());
        // Convert lidvids to lids
        List<String> lids = LidVidUtils.lidvidToLid(batch.getLidVids());
        XmlDocUtils.writeField(writer, "product_lid", lids);
        
        // Lid refs
        XmlDocUtils.writeField(writer, "product_lid", batch.getLids());
        
        // Transaction ID
        XmlDocUtils.writeField(writer, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        writer.append("</doc>\n");
    }


    @Override
    public void close() throws Exception
    {
        writer.append("</add>\n");
        writer.close();
    }
}
