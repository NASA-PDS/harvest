package gov.nasa.pds.harvest.util;

/**
 * Helper methods to work with strings.
 * 
 * @author karpenko
 */
public class PdsStringUtils
{
    /**
     * Trim a string. Return null if the string is empty.
     * @param str a string
     * @return trimmed string
     */
    public static String trim(String str)
    {
        if(str == null) return null;
        str = str.trim();
        
        return str.isEmpty() ? null : str;
    }

}
