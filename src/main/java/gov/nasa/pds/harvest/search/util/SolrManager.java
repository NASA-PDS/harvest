package gov.nasa.pds.harvest.search.util;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;


public class SolrManager
{
    private static SolrManager instance;
    private SolrClient solrClient;
    
    
    private SolrManager(String url)
    {
        solrClient = new HttpSolrClient.Builder(url).build();
    }

    
    public static void init(String url)
    {
        if(instance != null) throw new RuntimeException("Already initialized.");
        instance = new SolrManager(url);
    }
    
    
    public static void destroy()
    {
        if(instance == null) return;
        
        try
        {
            instance.getSolrClient().close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }

    
    public static SolrManager getInstance()
    {
        return instance;
    }
    
    
    public SolrClient getSolrClient()
    {
        return solrClient;
    }

}
