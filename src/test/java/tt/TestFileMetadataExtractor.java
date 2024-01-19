package tt;

import java.io.File;

import gov.nasa.pds.harvest.cfg.ConfigManager;
import gov.nasa.pds.harvest.cfg.model.Configuration;
import gov.nasa.pds.registry.common.meta.FileMetadataExtractor;
import gov.nasa.pds.registry.common.meta.Metadata;


public class TestFileMetadataExtractor
{

    public static void main(String[] args) throws Exception
    {
        ConfigManager cfgReader = new ConfigManager();
        Configuration cfg = cfgReader.read(new File("/tmp/harvest.xml"));
        FileMetadataExtractor extractor = new FileMetadataExtractor();

        File file = new File("/tmp/d1/atlas_document.xml");
        Metadata meta = new Metadata();
        extractor.extract(file, meta, cfg.fileInfo.fileRef);
        
        for(String fieldName: meta.fields.getNames())
        {
            System.out.println(fieldName + " : " + meta.fields.getValues(fieldName));
        }
    }

}
