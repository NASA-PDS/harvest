package gov.nasa.pds.harvest.util.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;


/**
 * Convert ISO dates to ISO instant format.
 * 
 * @author karpenko
 */
public class IsoDateTimeConverter
{
    private DateTimeFormatter DATE_TIME; 
    
    public IsoDateTimeConverter()
    {
        DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()            
                // Date
                .append(DateTimeFormatter.ISO_LOCAL_DATE)            
                // Time
                .appendLiteral('T')
                .append(DateConstants.OPTIONAL_TIME)           
                // Optional time zone
                .optionalStart()
                .appendZoneRegionId()
                .optionalEnd()            
                .toFormatter();
    }


    /**
     * Convert ISO dates to ISO instant format.
     * @param value ISO date
     * @return ISO instant
     */
    public Instant toInstant(String value)
    {
        if(value == null) return null;
        
        TemporalAccessor tmp = DATE_TIME.parseBest(value, ZonedDateTime::from, LocalDateTime::from);
        
        if(tmp instanceof ZonedDateTime)
        {
            return ZonedDateTime.from(tmp).toInstant();
        }
        else if(tmp instanceof LocalDateTime)
        {
            return LocalDateTime.from(tmp).toInstant(ZoneOffset.UTC);
        }
        else
        {
            return null;
        }        
    }

}
