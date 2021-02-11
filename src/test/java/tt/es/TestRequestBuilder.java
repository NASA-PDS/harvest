package tt.es;

import java.util.Set;
import java.util.TreeSet;

import gov.nasa.pds.harvest.dao.EsRequestBuilder;

public class TestRequestBuilder
{

    public static void main(String[] args) throws Exception
    {
        EsRequestBuilder bld = new EsRequestBuilder(true);
        
        Set<String> ids = new TreeSet<>();
        ids.add("id123");
        
        String json = bld.createSearchIdsRequest(ids, 100);
        System.out.println(json);
    }

}
