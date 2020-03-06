package gov.nasa.pds.harvest.util;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;


public class FileDataUtils
{
    public static void setFileContent(FileData fd, File file) throws Exception
    {
        // Read the file content in memory
        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        // Calculate MD5 hash
        byte[] md5hash = MessageDigest.getInstance("MD5").digest(fileContent);
        fd.md5Base64 = Base64.getEncoder().encodeToString(md5hash);
        
        // Base64 encode file content to store in Solr binary field
        fd.contentBase64 = Base64.getEncoder().encodeToString(fileContent);
    }

}
