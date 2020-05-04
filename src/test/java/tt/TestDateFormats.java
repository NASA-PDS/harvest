package tt;

import gov.nasa.pds.harvest.util.solr.PDSDateConverter;


public class TestDateFormats
{

    public static void main(String[] args) throws Exception
    {        
        testPdsDates();
    }

    
    private static void testPdsDates()
    {
        testPdsDate("2013-10-24T00:00:00Z");
        testPdsDate("2013-10-24T00:49:37.457Z");
        
        testPdsDate("2013-10-24T01");
        testPdsDate("2013-302T01:02:03.123");
        
        testPdsDate("20130302010203.123");
    }

    
    private static void testPdsDate(String value)
    {
        String solrValue = PDSDateConverter.toSolrDateString("", value);
        System.out.format("%30s  -->  %s\n", value, solrValue);
    }
}
