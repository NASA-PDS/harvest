package gov.nasa.pds.harvest.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.json.JSONObject;
import org.json.XML;

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
    private Logger log;
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
        log = LogManager.getLogger(this.getClass());
        
        this.fileInfoCfg = config.fileInfo;
        if(this.fileInfoCfg == null)
        {
            throw new Exception("File Info configuration is null.");
        }

        if(fileInfoCfg.storeLabels == false)
        {
            log.warn("XML BLOB storage is disabled "
                    + "(see <fileInfo storeLabels=\"false\"> configuration). "
                    + "Not all Registry features will be available.");
        }

        if(fileInfoCfg.storeJsonLabels == false)
        {
            log.warn("JSON BLOB storage is disabled "
                    + "(see <fileInfo storeJsonLabels=\"false\"> configuration). "
                    + "Not all Registry features will be available.");
        }
        
        md5Digest = MessageDigest.getInstance("MD5");
        buf = new byte[1024 * 16];
        tika = new Tika();
    }

    
    /**
     * Extract file metadata
     * @param file a file
     * @param meta extracted metadata is added to this object
     * @throws Exception an exception
     */
    public void extract(File file, Metadata meta) throws Exception
    {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        String dt = attr.creationTime().toInstant().truncatedTo(ChronoUnit.SECONDS).toString();
        meta.fields.addValue(createLabelFileFieldName("creation_date_time"), dt);
        
        meta.fields.addValue(createLabelFileFieldName("file_name"), file.getName());
        meta.fields.addValue(createLabelFileFieldName("file_size"), String.valueOf(file.length()));
        meta.fields.addValue(createLabelFileFieldName("md5_checksum"), getMd5(file));
        meta.fields.addValue(createLabelFileFieldName("file_ref"), getFileRef(file));
        
        // XML BLOB (optional)
        if(fileInfoCfg.storeLabels)
        {
            meta.fields.addValue(createLabelFileFieldName("blob"), getBlob(file));
        }
        
        // JSON BLOB (required)
        if(fileInfoCfg.storeJsonLabels)
        {
            meta.fields.addValue(createLabelFileFieldName("json_blob"), getJsonBlob(file));
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
    
    
    /**
     * Calculate MD5 hash of a file
     * @param file a file
     * @return HEX encoded MD5 hash
     * @throws Exception an exception
     */
    public String getMd5(File file) throws Exception
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
    
    
    /**
     * Deflate (zip) PDS XML label and then Base64 encode.
     * @param file PDS XML label
     * @return Base64 encoded string
     * @throws Exception an exception
     */
    public String getBlob(File file) throws Exception
    {
        FileInputStream source = null;
        
        // Zipped content holder
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        // Zipper
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


    /**
     * Convert PDS XML label into JSON, deflate (zip) and then Base64 encode.
     * @param file PDS XML label
     * @return Base64 encoded string
     * @throws Exception an exception
     */
    public static String getJsonBlob(File file) throws Exception
    {
        Reader source = null;
        
        // Zipped content holder
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        // Zipper
        DeflaterOutputStream deflater = new DeflaterOutputStream(bas);
        // Writer to output stream adapter
        OutputStreamWriter dest = new OutputStreamWriter(deflater);
        
        try
        {
            source = new FileReader(file);
            JSONObject json = XML.toJSONObject(source);
            String strJson = json.toString();
            dest.write(strJson);
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
