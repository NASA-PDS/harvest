package gov.nasa.pds.harvest.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.binary.Hex;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.util.CloseUtils;


public class FileMetadataExtractor
{
    private FileInfoCfg fileInfoCfg;
    
    private MessageDigest md5Digest;
    private byte[] buf;

    private boolean storeBlob;
    
    
    public FileMetadataExtractor(Configuration config) throws Exception
    {
        this.fileInfoCfg = config.fileInfo;
        if(fileInfoCfg != null)
        {
            this.storeBlob = (config.fileInfo.blobStorageType == FileInfoCfg.BLOB_EMBEDDED);
            
            md5Digest = MessageDigest.getInstance("MD5");
            buf = new byte[1024 * 16];
        }
    }
    
    
    public void extract(File file, Metadata meta) throws Exception
    {
        if(fileInfoCfg == null) return;

        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        meta.fields.addValue("ops/Label_File_Info/ops/file_name", file.getName());
        meta.fields.addValue("ops/Label_File_Info/ops/creation_date_time", attr.creationTime().toInstant().toString());
        meta.fields.addValue("ops/Label_File_Info/ops/file_size", String.valueOf(file.length()));
        meta.fields.addValue("ops/Label_File_Info/ops/md5_checksum", getMd5(file));
        meta.fields.addValue("ops/Label_File_Info/ops/file_ref", getFileRef(file));
        
        if(storeBlob)
        {
            meta.fields.addValue("ops/Label_File_Info/ops/blob", getBlob(file));
        }
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
            return Hex.encodeHexString(hash);
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


    private String getFileRef(File file)
    {
        String filePath = file.toURI().getPath();
        
        if(fileInfoCfg.fileRef != null)
        {
            for(FileInfoCfg.FileRefCfg rule: fileInfoCfg.fileRef)
            {
                if(filePath.startsWith(rule.prefix))
                {
                    filePath = rule.replacement + filePath.substring(rule.prefix.length());
                    break;
                }
            }
        }
        
        return filePath;
    }

}
