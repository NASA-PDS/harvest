package tt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class TestZip
{

    public static void main(String[] args) throws Exception
    {
        //String fileName = "/tmp/harvest/data/housekeeping_13297_13304.xml";
        
        String fileName = "/tmp/harvest/data/collection_ldex_data_calibrated.xml"; 
        
        deflate(fileName);
        //gzip(fileName);
        
        //testExport();
    }

    
    
    private static void deflate(String fileName) throws Exception
    {
        FileInputStream fis = new FileInputStream(fileName);
        
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION);        
        FileOutputStream fos = new FileOutputStream("/tmp/deflate.dat");
        DeflaterOutputStream dos = new DeflaterOutputStream(fos, def);

        copy(fis, dos);
        
        dos.close();
        fis.close();
    }

    
    private static void copy(InputStream source, OutputStream dest) throws Exception
    {
        byte[] buf = new byte[1024];
        int count = 0;

        while((count = source.read(buf)) >= 0)
        {
            dest.write(buf, 0, count);
        }
    }
}
