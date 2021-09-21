package gov.nasa.pds.harvest.util;

import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * Utility class to work with Jar manifest files.
 * 
 * @author karpenko
 */
public class ManifestUtils
{
    /**
     * Get manifest attributes from the manifest of a JAR this class is in.
     * If this class is not in a JAR, e.g., this code is ran from Eclipse, return null.
     * @return Manifest attributes or null.
     */
    public static Attributes getAttributes()
    {
        URL url = ManifestUtils.class.getResource(ManifestUtils.class.getSimpleName() + ".class");
        
        if(url == null) return null;
        String classPath = url.toString();        
        if(classPath == null || !classPath.startsWith("jar:")) return null;
        
        InputStream is = null;
        
        try
        {
            String manifestPath = classPath.substring(0, classPath.indexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            
            is = new URL(manifestPath).openStream();
            if(is == null) return null;
            
            Manifest manifest = new Manifest(is);
            Attributes attrs = manifest.getMainAttributes();
            return attrs;
        }
        catch(Exception ex)
        {
            // Ignore
        }
        finally
        {
            if(is != null) CloseUtils.close(is);
        }
        
        return null;
    }
}
