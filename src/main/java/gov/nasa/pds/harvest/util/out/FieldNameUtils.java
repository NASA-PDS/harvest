package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.Constants;

/**
 * Utility class to work with fields
 * 
 * @author karpenko
 */
public class FieldNameUtils
{
    /**
     * Create full field name / ID.
     * @param ns namespace
     * @param claz class name
     * @param attr attribute name
     * @return field name
     */
    public static String createFieldName(String ns, String claz, String attr)
    {
        String nsPrefix = ns + Constants.NS_SEPARATOR;
        return nsPrefix + claz + Constants.ATTR_SEPARATOR + nsPrefix + attr;
    }

}
