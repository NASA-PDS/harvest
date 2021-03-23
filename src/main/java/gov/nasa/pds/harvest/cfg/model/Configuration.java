package gov.nasa.pds.harvest.cfg.model;

import java.util.List;


public class Configuration
{
    public List<BundleCfg> bundles;
    public List<String> dirs;
    
    public FiltersCfg filters;
    
    public FileInfoCfg fileInfo;
    public XPathMapCfg xpathMaps;
    public AutogenCfg autogen;
    
    public RegistryCfg registryCfg;
    
    public RefsCfg refsCfg;
}
