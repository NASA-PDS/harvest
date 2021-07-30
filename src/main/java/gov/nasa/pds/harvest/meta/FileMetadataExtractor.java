package gov.nasa.pds.harvest.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.tika.Tika;

import gov.nasa.pds.harvest.cfg.model.FileInfoCfg;
import gov.nasa.pds.harvest.Constants;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.harvest.util.CloseUtils;
import gov.nasa.pds.harvest.util.out.FieldNameUtils;

/**
 * Extracts file metadata, such as file name, size, checksum.
 * @author karpenko
 */
public class FileMetadataExtractor
{
    private FileInfoCfg fileInfoCfg;
    
    private MessageDigest md5Digest;
    private byte[] buf;
    private Tika tika;

    
    /**
     * Constructor
     * @param config configuration
     * @throws Exception and exception
     */
    public FileMetadataExtractor(Configuration config) throws Exception
    {
        this.fileInfoCfg = config.fileInfo;

        if(this.fileInfoCfg != null)
        {
            md5Digest = MessageDigest.getInstance("MD5");
            buf = new byte[1024 * 16];
            tika = new Tika();
        }
    }

    
    /**
     * Extract file metadata
     * @param file a file
     * @param meta extracted metadata is added to this object
     * @throws Exception an exception
     */
    public void extract(File file, Metadata meta) throws Exception
    {
        if(fileInfoCfg == null) return;

        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        String dt = attr.creationTime().toInstant().truncatedTo(ChronoUnit.SECONDS).toString();
        meta.fields.addValue(createLabelFileFieldName("creation_date_time"), dt);
        
        meta.fields.addValue(createLabelFileFieldName("file_name"), file.getName());
        meta.fields.addValue(createLabelFileFieldName("file_size"), String.valueOf(file.length()));
        meta.fields.addValue(createLabelFileFieldName("md5_checksum"), getMd5(file));
        meta.fields.addValue(createLabelFileFieldName("file_ref"), getFileRef(file));
        
        if(fileInfoCfg.storeLabels)
        {
            meta.fields.addValue(createLabelFileFieldName("blob"), getBlob(file));
        }
        
        // Process data files
        if(fileInfoCfg.processDataFiles)
        {
            processDataFiles(file.getParentFile(), meta);
        }
    }
    
    
    private static String createLabelFileFieldName(String attr)
    {
        return FieldNameUtils.createFieldName(Constants.REGISTRY_NS, "Label_File_Info", attr);
    }

    
    private static String createDataFileFieldName(String attr)
    {
        return FieldNameUtils.createFieldName(Constants.REGISTRY_NS, "Data_File_Info", attr);
    }

    
    private void processDataFiles(File baseDir, Metadata meta) throws Exception
    {
        if(meta == null || meta.dataFiles == null) return;
        
        for(String fileName: meta.dataFiles)
        {
            File file = new File(baseDir, fileName);
            if(!file.exists())
            {
                throw new Exception("Data file " + file.getAbsolutePath() + " doesn't exist");
            }
            
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            String dt = attr.creationTime().toInstant().truncatedTo(ChronoUnit.SECONDS).toString();
            meta.fields.addValue(createDataFileFieldName("creation_date_time"), dt);
            
            meta.fields.addValue(createDataFileFieldName("file_name"), file.getName());            
            meta.fields.addValue(createDataFileFieldName("file_size"), String.valueOf(file.length()));
            meta.fields.addValue(createDataFileFieldName("md5_checksum"), getMd5(file));
            meta.fields.addValue(createDataFileFieldName("file_ref"), getFileRef(file));
            meta.fields.addValue(createDataFileFieldName("mime_type"), getMimeType(file));
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

    
    private String getMimeType(File file) throws Exception
    {
        InputStream is = null;
        
        try
        {
            is = new FileInputStream(file);
            return tika.detect(is);
        }
        finally
        {
            CloseUtils.close(is);
        }
    }
}
