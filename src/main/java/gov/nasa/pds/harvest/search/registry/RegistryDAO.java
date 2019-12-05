package gov.nasa.pds.harvest.search.registry;

import java.io.File;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import gov.nasa.pds.harvest.search.util.SolrManager;
import gov.nasa.pds.harvest.search.util.TransactionManager;

/**
 * Data Access Object to work with the registry collection in Solr.
 * @author karpenko
 */
public class RegistryDAO
{
    private static final String SOLR_REGISTRY = "registry";
    
    private FileDataLoader fileLoader;
    
    public RegistryDAO()
    {
        fileLoader = new FileDataLoader();
    }
    
    public boolean hasProduct(String lid, String vid) throws Exception 
    {
        String lidvid = lid + "::" + vid;

        SolrQuery query = new SolrQuery("lidvid:\"" + lidvid + "\"");
        
        SolrClient client = SolrManager.getInstance().getSolrClient();
        QueryResponse response = client.query(SOLR_REGISTRY, query);
        
        SolrDocumentList documents = response.getResults();
        return (documents.getNumFound() != 0); 
    }

    
    public void saveProduct(RegistryMetadata meta, File file) throws Exception
    {
        String lidvid = meta.lid + "::" + meta.vid;
        FileData fileData = fileLoader.load(file);
        
        final SolrInputDocument doc = new SolrInputDocument();

        // Ids
        doc.addField("lid", meta.lid);
        doc.addField("vid", meta.vid);       
        doc.addField("lidvid", lidvid);

        // File Info
        doc.addField("file_name", fileData.name);
        doc.addField("file_type", fileData.mimeType);
        doc.addField("file_size", fileData.size);

        // File content
        doc.addField("content", fileData.contentBase64);
        doc.addField("md5", fileData.md5Base64);
        
        // Transaction ID
        doc.addField("package_id", TransactionManager.getInstance().getTransactionId());
        
        // Metadata
        addField(doc, "product_class", meta.productClass);
        addField(doc, "investigation_name", meta.investigation);
        addField(doc, "instrument_name", meta.instrument);
        addField(doc, "instrument_host_name", meta.instrumentHost);
        addField(doc, "target_name", meta.target);
        
        // Save the document
        SolrClient client = SolrManager.getInstance().getSolrClient();
        client.add(SOLR_REGISTRY, doc);
        client.commit(SOLR_REGISTRY);
    }
        
    
    private static void addField(SolrInputDocument doc, String name, Object value)
    {
        if(value == null) return;
        doc.addField(name, value);
    }

}
