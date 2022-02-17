package gov.nasa.pds.harvest.cfg.model;

import java.util.List;

import gov.nasa.pds.registry.common.cfg.RegistryCfg;


/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class Configuration
{
    public String nodeName;
    
    public List<BundleCfg> bundles;
    public List<String> dirs;
    public List<String> manifests;
    
    public FiltersCfg filters;
    
    public FileInfoCfg fileInfo;
    public XPathMapCfg xpathMaps;
    public AutogenCfg autogen;
    
    public RegistryCfg registryCfg;
    
    public RefsCfg refsCfg;
}
