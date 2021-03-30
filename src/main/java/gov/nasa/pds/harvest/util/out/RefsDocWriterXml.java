package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import gov.nasa.pds.harvest.crawler.ProdRefsBatch;
import gov.nasa.pds.harvest.crawler.RefType;
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
    public void writeBatch(Metadata meta, ProdRefsBatch batch, RefType refType) throws Exception
    {
        String id = meta.lidvid + "::" + refType.getId() + batch.batchNum;
        
        writer.append("<doc>\n");
        
        // Document id
        XmlDocUtils.writeField(writer, "_id", id);
        
        // Batch info
        XmlDocUtils.writeField(writer, "batch_id", String.valueOf(batch.batchNum));
        XmlDocUtils.writeField(writer, "batch_size", String.valueOf(batch.size));
        
        // Reference type
        XmlDocUtils.writeField(writer, "reference_type", refType.getLabel());

        // Collection ids
        XmlDocUtils.writeField(writer, "collection_lidvid", meta.lidvid);
        XmlDocUtils.writeField(writer, "collection_lid", meta.lid);
        XmlDocUtils.writeField(writer, "collection_vid", meta.vid);
        
        // LidVid refs
        XmlDocUtils.writeField(writer, "product_lidvid", batch.lidvids);
        // Convert lidvids to lids
        XmlDocUtils.writeField(writer, "product_lid", LidVidUtils.lidvidToLid(batch.lidvids));
        // Lid refs
        XmlDocUtils.writeField(writer, "product_lid", batch.lids);
        
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
