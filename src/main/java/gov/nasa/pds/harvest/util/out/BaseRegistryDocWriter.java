package gov.nasa.pds.harvest.util.out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.pds.harvest.Constants;
import gov.nasa.pds.registry.common.util.xml.XmlNamespaces;

/**
 * Interface to write metadata extracted from PDS4 label.
 *  
 * @author karpenko
 */
public abstract class BaseRegistryDocWriter implements RegistryDocWriter
{
    protected File outDir;
    protected Writer writer;

    protected Set<String> missingFields;
    protected Map<String, String> missingXsds;

    
    /**
     * Constructor
     * @param outDir output directory
     * @throws Exception Generic exception
     */
    public BaseRegistryDocWriter(File outDir) throws Exception
    {
        this.outDir = outDir;
        missingFields = new HashSet<>();
        missingXsds = new HashMap<>();
    }

    
    protected void updateMissingXsds(String name, XmlNamespaces xmlns)
    {
        int idx = name.indexOf(Constants.NS_SEPARATOR);
        if(idx <= 0) return;
        
        String prefix = name.substring(0, idx);
        String xsd = xmlns.prefix2location.get(prefix);
 
        if(xsd != null)
        {
            missingXsds.put(xsd, prefix);
        }
    }

    
    protected void saveMissingFields() throws IOException
    {
        File file = new File(outDir, "missing_fields.txt");
        FileWriter wr = new FileWriter(file);
        
        for(String field: missingFields)
        {
            field = NDJsonDocUtils.toEsFieldName(field);
            wr.write(field);
            wr.write("\n");
        }
        
        wr.close();
    }

    
    protected void saveMissingXsds() throws IOException
    {
        File file = new File(outDir, "missing_xsds.txt");
        FileWriter wr = new FileWriter(file);
        
        for(Map.Entry<String, String> item: missingXsds.entrySet())
        {
            // <prefix>;<location>
            wr.write(item.getValue() + ";" + item.getKey());
            wr.write("\n");
        }
        
        wr.close();
    }

}
