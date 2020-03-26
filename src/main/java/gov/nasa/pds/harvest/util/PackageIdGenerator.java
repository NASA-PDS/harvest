package gov.nasa.pds.harvest.util;

import java.util.UUID;

/**
 * Singleton to generate package ids.
 * 
 * @author karpenko
 */
public class PackageIdGenerator
{
    private static PackageIdGenerator instance = new PackageIdGenerator();
    private String packageId;

    
    private PackageIdGenerator()
    {
        reset();
    }

    
    public static PackageIdGenerator getInstance()
    {
        return instance;
    }

    
    public void reset()
    {
        packageId = UUID.randomUUID().toString();
    }
    
    
    public String getPackageId()
    {
        return packageId;
    }
}
