package gov.nasa.pds.harvest.policy.model;


public class Policy
{
    protected Directory directories;
    protected AccessUrls accessUrls;

    
    public Policy()
    {
        directories = new Directory();
    }
    
    
    public Directory getDirectories() 
    {
        return directories;
    }
    
    public AccessUrls getAccessUrls()
    {
        return accessUrls;
    }
}
