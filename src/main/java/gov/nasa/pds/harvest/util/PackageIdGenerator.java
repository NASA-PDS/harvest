package gov.nasa.pds.harvest.util;

import java.util.UUID;

/**
 * Singleton to generate package / Harvest run IDs.
 * 
 * @author karpenko
 */
public class PackageIdGenerator
{
    private static PackageIdGenerator instance = new PackageIdGenerator();
    private String packageId;

    
    /**
     * Private constructor. Use getInstance() instead.
     */
    private PackageIdGenerator()
    {
        reset();
    }

    
    /**
     * Get the singleton instance.
     * @return Singleton instance
     */
    public static PackageIdGenerator getInstance()
    {
        return instance;
    }

    
    /**
     * Reset / create new ID.
     */
    public void reset()
    {
        packageId = UUID.randomUUID().toString();
    }
    
    
    /**
     * Get package / Harvest run ID.
     * @return Return the same ID, until reset() is called. 
     * Usually one ID per Harvest run is used.
     */
    public String getPackageId()
    {
        return packageId;
    }
}
