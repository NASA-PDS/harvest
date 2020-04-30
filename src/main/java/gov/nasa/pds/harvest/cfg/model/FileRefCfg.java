package gov.nasa.pds.harvest.cfg.model;

import java.util.List;


public class FileRefCfg
{
    public static class ReplaceRule
    {
        public String prefix;
        public String replacement;
    }

    public List<ReplaceRule> rules;
}
