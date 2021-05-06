package gov.nasa.pds.harvest.cfg.parser;

import java.util.Map;
import java.util.TreeMap;


/**
 * Validates node name value in Harvest configuration file.
 *  
 * @author karpenko
 */
public class NodeNameValidator
{
    private static final String ERROR = "Invalid Harvest configuration: ";
    
    private Map<String, String> map;
    
    /**
     * Constructor
     */
    public NodeNameValidator()
    {
        map = new TreeMap<>();

        map.put("PDS_ATM", "Planetary Data System: Atmospheres Node");
        map.put("PDS_ENG", "Planetary Data System: Engineering Node");
        map.put("PDS_GEO", "Planetary Data System: Geosciences Node");
        map.put("PDS_IMG", "Planetary Data System: Imaging Node");
        map.put("PDS_NAIF","Planetary Data System: NAIF Node");
        map.put("PDS_PPI", "Planetary Data System: Planetary Plasma Interactions Node");
        map.put("PDS_RMS", "Planetary Data System: Rings Node");
        map.put("PDS_SBN", "Planetary Data System: Small Bodies Node at University of Maryland");
        map.put("PSA",  "Planetary Science Archive");
        map.put("JAXA", "Japan Aerospace Exploration Agency");
        map.put("ROSCOSMOS", "Russian State Corporation for Space Activities");
    }
    
    
    /**
     * Validate node name
     * @param id
     * @throws Exception
     */
    public void validate(String id) throws Exception
    {
        if(id == null) throw new Exception(ERROR + "'harvest' element is missing required attribute 'nodeName'");
        
        if(!map.containsKey(id))
        {
            StringBuilder buf = new StringBuilder();
            buf.append(ERROR);
            buf.append("'/harvest@nodeName' attribute has invalid value: '" + id + "'.\n");
            buf.append("Please use one of the following values:\n");
            map.forEach((key, value) -> {
                buf.append(String.format("    %-9s - %s\n", key, value));
            });
            
            throw new Exception(buf.toString());
        }
    }
}
