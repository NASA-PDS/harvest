package gov.nasa.pds.harvest.cfg.policy.model;

import java.util.ArrayList;
import java.util.List;


public class AccessUrls
{
    protected List<AccessUrl> accessUrl;
    protected boolean registerFileUrls;

    
    public AccessUrls()
    {
        accessUrl = new ArrayList<>();
    }

    
    public List<AccessUrl> getAccessUrl()
    {
        return this.accessUrl;
    }


    public boolean isRegisterFileUrls()
    {
        return registerFileUrls;
    }


    public void setRegisterFileUrls(boolean value)
    {
        this.registerFileUrls = value;
    }

}
