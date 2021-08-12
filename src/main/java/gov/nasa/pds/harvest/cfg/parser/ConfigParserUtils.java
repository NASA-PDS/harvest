package gov.nasa.pds.harvest.cfg.parser;

/**
 * Utility methods for parsing Harvest configuration file.
 * @author karpenko
 */
public class ConfigParserUtils
{
    /**
     * Parse boolean string. The following case insensitive values are supported:
     * "yes", "true", "no", "false".
     * @param str A string to parse
     * @return true if the string = "yes" or "true"; false if the string = "no" or "false";
     * null if the string has another value.
     */
    public static Boolean parseBoolean(String str)
    {
        if(str == null) return null;
        
        if(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes")) return true;
        if(str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no")) return false;
        
        return null;
    }
}
