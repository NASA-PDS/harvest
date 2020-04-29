package gov.nasa.pds.harvest.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.util.CloseUtils;


public class FileDataExtractor
{
    private MessageDigest md5Digest;
    private byte[] buf;

    private boolean extractFileInfo;
    private boolean storeBlob;
    
    
    public FileDataExtractor(Configuration config) throws Exception
    {
        if(config.fileInfo == null)
        {
            this.extractFileInfo = false;
            this.storeBlob = false;
        }
        else
        {
            this.extractFileInfo = true;
            this.storeBlob = (config.fileInfo.blobStorageType == FileInfoCfg.BLOB_EMBEDDED);
            
            md5Digest = MessageDigest.getInstance("MD5");
            buf = new byte[1024 * 2];
        }
    }
    
    
    public FileData extract(File file) throws Exception
    {
        return extract(file, "application/xml");
    }
    
    
    public FileData extract(File file, String mimeType) throws Exception
    {
        if(!extractFileInfo) return null;
        
        FileData data = new FileData();
        
        data.name = file.getName();
        data.size = file.length();
        data.mimeType = mimeType;

        data.md5Base64 = getMd5(file);
        if(storeBlob)
        {
            data.blobBase64 = getBlob(file);
        }
        
        return data;
    }
    

    private String getMd5(File file) throws Exception
    {
        md5Digest.reset();
        FileInputStream source = null;
        
        try
        {
            source = new FileInputStream(file);
            
            int count = 0;
            while((count = source.read(buf)) >= 0)
            {
                md5Digest.update(buf, 0, count);
            }
            
            byte[] hash = md5Digest.digest();
            return Base64.getEncoder().encodeToString(hash);
        }
        finally
        {
            CloseUtils.close(source);
        }
    }
    
    
    private String getBlob(File file) throws Exception
    {
        FileInputStream source = null;
        
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DeflaterOutputStream dest = new DeflaterOutputStream(bas);
        
        try
        {
            source = new FileInputStream(file);
            
            int count = 0;
            while((count = source.read(buf)) >= 0)
            {
                dest.write(buf, 0, count);
            }
            
            dest.close();
            return Base64.getEncoder().encodeToString(bas.toByteArray());
        }
        finally
        {
            CloseUtils.close(source);
        }
    }

}
