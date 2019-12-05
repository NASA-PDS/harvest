package gov.nasa.pds.harvest.search.registry;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;

import org.apache.tika.Tika;

public class FileDataLoader
{
    private Tika tika;
    
    public FileDataLoader()
    {
        tika = new Tika();
    }
    
    public FileData load(File file) throws Exception
    {
        FileData data = new FileData();

        data.name = file.getName();
        data.mimeType = tika.detect(file);
        data.size = file.length();

        // Read the file content in memory
        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        // Calculate MD5 hash
        byte[] md5hash = MessageDigest.getInstance("MD5").digest(fileContent);
        data.md5Base64 = Base64.getEncoder().encodeToString(md5hash);
        
        // Base64 encode file content to store in Solr binary field
        data.contentBase64 = Base64.getEncoder().encodeToString(fileContent);
        
        return data;        
    }
}
