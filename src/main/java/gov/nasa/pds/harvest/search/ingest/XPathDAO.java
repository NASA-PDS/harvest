package gov.nasa.pds.harvest.search.ingest;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;

import javax.ws.rs.core.MediaType;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.json.JSONObject;
import org.json.XML;

import gov.nasa.pds.harvest.search.util.SolrManager;
import gov.nasa.pds.harvest.search.util.TransactionManager;


/**
 * Data Access Object to work with the xpath collection in Solr.
 * @author karpenko
 */
public class XPathDAO
{
    public static void postXPaths(File prodFile, String lid, String vid) throws Exception 
    {
        String endPoint = "/xpath/update/json/docs";
        ContentStreamUpdateRequest up = new ContentStreamUpdateRequest(endPoint);
        BufferedReader br = null;

        try 
        {
            br = new BufferedReader(new FileReader(prodFile));
            
            // Convert XML to JSON
            JSONObject json = XML.toJSONObject(br);
            
            // Add extra fields
            String lidvid = lid + "::" + vid;
            json.append("id", lidvid);
            json.append("package_id", TransactionManager.getInstance().getTransactionId());
            
            // Store JSON in Solr
            StringStream stringStream = new StringStream(json.toString(2), MediaType.APPLICATION_JSON);
            up.addContentStream(stringStream);
            up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
            
            SolrClient client = SolrManager.getInstance().getSolrClient();
            NamedList<Object> list = client.request(up);
        } 
        finally 
        {
            closeQuietly(br);
        }
    }

    
    private static void closeQuietly(Closeable cl)
    {
        if(cl == null) return;
        
        try
        {
            cl.close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }
}
