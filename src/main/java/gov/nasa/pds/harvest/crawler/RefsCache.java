package gov.nasa.pds.harvest.crawler;


public class RefsCache
{
    private static RefsCache instance = new RefsCache();

    private LidVidCache prodRefsCache;
    private LidVidCache collectionRefsCache;
    
    
    private RefsCache()
    {
        prodRefsCache = new LidVidCache();
        collectionRefsCache = new LidVidCache();
    }

    
    public static RefsCache getInstance()
    {
        return instance;
    }

    
    public LidVidCache getProdRefsCache()
    {
        return prodRefsCache;
    }


    public LidVidCache getCollectionRefsCache()
    {
        return collectionRefsCache;
    }

}
