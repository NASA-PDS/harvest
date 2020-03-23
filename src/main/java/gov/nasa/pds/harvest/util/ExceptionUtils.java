package gov.nasa.pds.harvest.util;


public class ExceptionUtils
{
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
