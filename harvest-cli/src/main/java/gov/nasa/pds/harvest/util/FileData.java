package gov.nasa.pds.harvest.util;

import java.io.File;


public class FileData
{
    public String name;
    public String mimeType;
    public long size;
    
    public String contentBase64;
    public String md5Base64;
    
    
    public FileData(File file, String mimeType)
    {
        this.name = file.getName();
        this.size = file.length();
        this.mimeType = mimeType;
    }
}
