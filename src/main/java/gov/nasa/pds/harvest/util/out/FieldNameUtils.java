package gov.nasa.pds.harvest.util.out;

import gov.nasa.pds.harvest.Constants;

public class FieldNameUtils
{
    public static String createFieldName(String ns, String claz, String attr)
    {
        String nsPrefix = ns + Constants.NS_SEPARATOR;
        return nsPrefix + claz + Constants.ATTR_SEPARATOR + nsPrefix + attr;
    }

}
