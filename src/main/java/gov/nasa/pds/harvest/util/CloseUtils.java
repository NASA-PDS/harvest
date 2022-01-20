package gov.nasa.pds.harvest.util;

import java.io.Closeable;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            Logger log = LogManager.getLogger(CloseUtils.class);
            log.warn(ex);
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
            Logger log = LogManager.getLogger(CloseUtils.class);
            log.warn(ex);
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
            Logger log = LogManager.getLogger(CloseUtils.class);
            log.warn(ex);
        }
    }
  
}
