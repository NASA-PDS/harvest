package gov.nasa.pds.harvest.util;

import java.io.Closeable;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;

/**
 * Close resources without throwing exceptions.
 * 
 * @author karpenko
 */
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

    
    public static void close(Stream<?> cl)
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


    public static void close(XMLEventReader cl)
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
