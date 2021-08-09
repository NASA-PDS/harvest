package tt.es;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.zip.InflaterInputStream;


/**
 * Few methods to export a BLOB to a file.
 * 
 * @author karpenko
 */
public class EmbeddedBlobExporter
{
    
    /**
     * Export Base64 encoded string into a file
     * @param blob Base64 encoded string
     * @param filePath output file path
     * @throws Exception an exception
     */
    public static void export(String blob, String filePath) throws Exception
    {
        byte[] data = Base64.getDecoder().decode(blob);
        export(data, filePath);
    }
    
    
    /**
     * Export binary data into a file
     * @param blob raw data
     * @param filePath output file path
     * @throws Exception an exception
     */
    public static void export(byte[] blob, String filePath) throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        InflaterInputStream source = new InflaterInputStream(bais);

        FileOutputStream dest = new FileOutputStream(filePath);
    
        copy(source, dest);
        dest.close();
    }


    /**
     * Copy data from input to output stream
     * @param source source stream
     * @param dest destination stream
     * @throws Exception an exception
     */
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
