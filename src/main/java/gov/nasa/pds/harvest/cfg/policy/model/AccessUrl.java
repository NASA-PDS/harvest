package gov.nasa.pds.harvest.cfg.policy.model;

import java.util.ArrayList;
import java.util.List;


public class AccessUrl
{
    protected String baseUrl;
    protected List<String> offset;

    
    public AccessUrl()
    {
        offset = new ArrayList<>();
    }
    
    
    public String getBaseUrl()
    {
        return baseUrl;
    }


    public void setBaseUrl(String value)
    {
        this.baseUrl = value;
    }


    public void addOffset(String str)
    {
        this.offset.add(str);
    }
    
    
    public List<String> getOffset()
    {
        return this.offset;
    }

}
