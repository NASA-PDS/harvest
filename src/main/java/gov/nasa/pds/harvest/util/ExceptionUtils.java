package gov.nasa.pds.harvest.util;


/**
 * Helper methods to work with exceptions.
 * 
 * @author karpenko
 */
public class ExceptionUtils
{
    /**
     * Extract original exception message from a stack trace. 
     * @param ex Exception object
     * @return Original error message
     */
    public static String getMessage(Exception ex)
    {
        if(ex == null) return "";
        
        Throwable tw = ex;
        while(tw.getCause() != null)
        {
            tw = tw.getCause();
        }
        
        return tw.getMessage();
    }

}
