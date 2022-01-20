package gov.nasa.pds.harvest.cfg.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Harvest configuration model.
 * 
 * @author karpenko
 */
public class AutogenCfg
{
    public Set<String> classFilterIncludes = new HashSet<>();
    public Set<String> classFilterExcludes = new HashSet<>();
    public Set<String> dateFields = new HashSet<>();
    public boolean generate = true;
}
