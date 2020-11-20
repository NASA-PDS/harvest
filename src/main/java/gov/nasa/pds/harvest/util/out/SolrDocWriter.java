package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.harvest.meta.Metadata;
import gov.nasa.pds.harvest.util.DocWriter;
import gov.nasa.pds.harvest.util.FieldMap;
import gov.nasa.pds.harvest.util.PackageIdGenerator;


/**
 * Solr document writer. 
 * Writes documents in XML format which can be loaded into Solr by Solr post tool. 
 *  
 * @author karpenko
 */
public class SolrDocWriter implements DocWriter
{
    private Writer writer;
    
    private boolean writeFields = true;
    private Set<String> allFields = new TreeSet<>();
    
    private File outDir;

    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception
     */
    public SolrDocWriter(File outDir) throws Exception
    {
        this.outDir = outDir;
        
        File file = new File(outDir, "solr-docs.xml");        
        writer = new FileWriter(file);
        
        writer.append("<add>\n");
    }

    
    public void setWriteFields(boolean b)
    {
        this.writeFields = b;
    }

    
    @Override
    public void close() throws Exception
    {
        writer.append("</add>\n");
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
            wr.write(field);
            wr.write("\n");
        }
        
        wr.close();        
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
    public void write(Metadata meta) throws Exception
    {
        writer.append("<doc>\n");

        // Basic info
        String lidvid = meta.lid + "::" + meta.vid;        
        SolrDocUtils.writeField(writer, "lid", meta.lid);
        SolrDocUtils.writeField(writer, "vid", meta.vid);
        SolrDocUtils.writeField(writer, "lidvid", lidvid);
        SolrDocUtils.writeField(writer, "title", meta.title);
        SolrDocUtils.writeField(writer, "product_class", meta.prodClass);
        
        // Transaction ID
        SolrDocUtils.writeField(writer, "_package_id", PackageIdGenerator.getInstance().getPackageId());
        
        // References
        write(meta.intRefs);
        
        // Other Fields
        write(meta.fields);
        
        writer.append("</doc>\n");
        
        // Build a list of all fields in all documents
        if(writeFields)
        {
            addFields(meta);
        }
    }
 
    
    private void write(FieldMap fmap) throws Exception
    {
        if(fmap == null || fmap.isEmpty()) return;
        
        for(String key: fmap.getNames())
        {
            Collection<String> values = fmap.getValues(key);
            for(String value: values)
            {
                SolrDocUtils.writeField(writer, key, value);
            }
        }
    }
}
