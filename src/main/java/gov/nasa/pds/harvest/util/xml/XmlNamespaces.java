package gov.nasa.pds.harvest.util.xml;

import java.util.Map;

/**
 * Mappings between LDD names (xmlns prefix), xmlns URIs and schema locations. 
 * @author karpenko
 */
public class XmlNamespaces
{
    /**
     * URI to prefix map. For example, 
     * "http://pds.nasa.gov/pds4/pds/v1" -&gt; "pds".
     */
    public Map<String, String> uri2prefix;
    
    /**
     * Prefix to schema location map. For example,
     * "pds" -&gt; "https://pds.nasa.gov/pds4/pds/v1/PDS4_PDS_1B00.xsd".
     */
    public Map<String, String> prefix2location;
    
    /**
     * Print mappings
     */
    public void debug()
    {
        System.out.println("===================================================");
        System.out.println(" URI to Prefix");
        System.out.println("===================================================");
        if(uri2prefix == null)
        {
            System.out.println("N/A");
        }
        else
        {
            uri2prefix.forEach((key, value) -> { 
                System.out.println(" " + key + "  -->  " + value);
            });
        }
        
        System.out.println("===================================================");
        System.out.println(" Prefix to Location");
        System.out.println("===================================================");
        if(prefix2location == null)
        {
            System.out.println("N/A");
        }
        else
        {
            prefix2location.forEach((key, value) -> { 
                System.out.println(" " + key + "  -->  " + value);
            });
        }
        
        System.out.println("===================================================");
    }

}
