package tt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tika.Tika;

import gov.nasa.pds.registry.common.util.CloseUtils;


public class TikaTest
{

    public static void main(String[] args) throws Exception
    {
        Tika tika = new Tika();
        
        InputStream is = null;
        
        try
        {
            is = new FileInputStream(new File("/ws2/SPICE/gen/naif0012.tls"));
            
            String mime = tika.detect(is);
            System.out.println(mime);
        }
        finally
        {
            CloseUtils.close(is);
        }
    }

}
