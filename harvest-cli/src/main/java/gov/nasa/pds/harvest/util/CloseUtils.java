package gov.nasa.pds.harvest.util;

import java.io.Closeable;

public class CloseUtils
{
    public static void close(Closeable cl)
    {
        if(cl == null) return;
        
        try
        {
            cl.close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }
}
