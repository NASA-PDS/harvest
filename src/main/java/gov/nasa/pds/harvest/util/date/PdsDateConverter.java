package gov.nasa.pds.harvest.util.date;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Utility class to convert dates from different PDS formats 
 * to "ISO instant" format. 
 * 
 * @author karpenko
 */
public class PdsDateConverter
{
    public static final String DEFAULT_STARTTIME = "1965-01-01T00:00:00.000Z";
    public static final String DEFAULT_STOPTIME = "3000-01-01T00:00:00.000Z";

    private static final Logger LOG = LogManager.getLogger(PdsDateConverter.class);
       
    private CompactDateTimeConverter compactDateTimeConverter;
    private DoyDateTimeConverter doyDateTimeConverter;
    private IsoDateTimeConverter isoDateTimeConverter;
    private LocalDateConverter localDateConverter;
    

    private boolean strict;
    
    /**
     * Constructor
     * @param strict if true, throw exception if a date could not be 
     * converted to ISO instant. If false, only print warning message.
     */
    public PdsDateConverter(boolean strict)
    {
        this.strict = strict;
        compactDateTimeConverter = new CompactDateTimeConverter();
        doyDateTimeConverter = new DoyDateTimeConverter();
        isoDateTimeConverter = new IsoDateTimeConverter();
        localDateConverter = new LocalDateConverter();
    }


    /**
     * Convert a date in one of PDS date formats to ISO instant string.
     * @param fieldName Metadata field name. Field name is used to return
     * default values for "start" and "stop" dates (e.g., mission 
     * "start_date_time" and "stop_date_time").
     * @param value a date in one of PDS date formats
     * @return ISO instant string
     * @throws Exception Generic exception
     */
    public String toIsoInstantString(String fieldName, String value) throws Exception
    {
        if(value == null) return null;
        
        if(value.isEmpty() 
                || value.equalsIgnoreCase("N/A") 
                || value.equalsIgnoreCase("UNK") 
                || value.equalsIgnoreCase("NULL")
                || value.equalsIgnoreCase("UNKNOWN"))
        {
            return getDefaultValue(fieldName);
        }

        // DateTime
        if(value.length() > 10)
        {
            String newValue = null;
            
            if(value.length() == 11 && value.endsWith("Z"))
            {
                newValue = convertDate(value.substring(0, 10));
                if(newValue != null) return newValue;
                
                handleInvalidDate(value);
                return value;
            }
            
            newValue = convertIsoDateTime(value);
            if(newValue != null) return newValue;

            newValue = convertCompactDateTime(value);
            if(newValue != null) return newValue;

            newValue = convertDoyTime(value);
            if(newValue != null) return newValue;
            
            handleInvalidDate(value);
        }
        // Date only
        else
        {
            String newValue = convertDate(value);
            if(newValue != null) return newValue;
            
            handleInvalidDate(value);
        }
        
        return value;
    }

    
    private void handleInvalidDate(String value) throws Exception
    {
        String msg = "Could not convert date " + value;
        
        if(strict)
        {
            throw new Exception(msg);
        }
        else
        {
            LOG.warn(msg);
        }
    }
    

    private String convertIsoDateTime(String value)
    {
        try
        {
            Instant inst = isoDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private String convertCompactDateTime(String value)
    {
        try
        {
            Instant inst = compactDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    private String convertDoyTime(String value)
    {
        try
        {
            Instant inst = doyDateTimeConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private String convertDate(String value)
    {
        try
        {
            Instant inst = localDateConverter.toInstant(value);
            return toInstantString(inst);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private static String toInstantString(Instant inst)
    {
        return (inst == null) ? null : DateTimeFormatter.ISO_INSTANT.format(inst);
    }

    
    public static String getDefaultValue(String fieldName)
    {
        if(fieldName == null) return null;
        
        if(fieldName.toLowerCase().contains("start"))
        {
            return DEFAULT_STARTTIME;
        }
        else
        {
            return DEFAULT_STOPTIME;
        }
    }

}
