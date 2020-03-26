package tt;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;


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

    
    
    private static void testExport() throws Exception
    {
        String data = "eJylVE2P2jAQvfMrrJzaQ+KEjSoUGa9oES0Su0JlW/UWWbEBS46NbPOx/76OHWgWShHdHBKN573nN8540OOhFmDHtOFKDqMsSSPAZKUol6th9ONlEg+iR9xDDSyuFWUCrDVbDqO1tRtTQLihJpHEkGSldk2QNy+4y+B8vMhL9yqzz2mamGod9QBwH1YT+7ph0gSNRmKrRaIqUSVKryA1VMAWp5X0u8+1otvKll+UtOxgnZCz01W4YiI6IgsX34E+GH5C7/f7ZP/grfXTNIO/nmYL7y7m0lgiK+Z5hhfB9ExVxPrDvLGdYwFw5zEeDI2wI6IpZdLyJQ97lSPNCPaCSKiVWxUlbxFM462WRSPenEJRhTMsLNErZgtiLNOK0yQvd8zVg+BfBIJy2yUugbMkQ7ATB4DlVjCcg59BKIQhxeVS6TqY9W1UtuxGynVdkiJ4HRM0Nm0XVIIYg896AsG3aU9xpJHgxJQzbuxx6biIERGudkksK4PVD/nHo/fzFIKBc1PjvfwnLpUGc0EksyB/h9DzaDoB0zFwTeue20pt2Dkq9OTGwKnFvrl1pV9P8DfJMbOEi46pupulblPcT7NBnGVxNkDwMvuHeavLPIgyU2m+afh4olUN3A0B393MMkC6zkkQ7CJOFf7D8//Vk36K+w931JPeVc/0+evC13NvOWepzq9D8MrkQC9+HrR8SerORfZRe8Xd6Majdma4K96EvYsSpJKXnhE8boHg2eXFvd+z3yS3";
        
        byte[] bb = Base64.getDecoder().decode(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(bb);
        InflaterInputStream source = new InflaterInputStream(bais);
        FileOutputStream dest = new FileOutputStream("/tmp/tmp.dat");
        copy(source, dest);
        dest.close();
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

    
    
    private static class MyZip extends GZIPOutputStream
    {
        public MyZip(OutputStream out) throws Exception
        {
            super(out);
            def.setLevel(Deflater.DEFAULT_COMPRESSION);
        }
    }
    
    
    private static void gzip(String fileName) throws Exception
    {
        FileInputStream fis = new FileInputStream(fileName);
        
        FileOutputStream fos = new FileOutputStream("/tmp/gzip.dat");
        GZIPOutputStream dos = new MyZip(fos);
        
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
