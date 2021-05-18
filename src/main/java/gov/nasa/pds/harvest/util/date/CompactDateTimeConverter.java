package gov.nasa.pds.harvest.util.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Parses / converts "yyyyMMddHHmmss" dates with optional nano seconds and time zone.
 * 
 * @author karpenko
 */
public class CompactDateTimeConverter
{
    private DateTimeFormatter DATE_TIME;
    
    /**
     * Constructor
     */
    public CompactDateTimeConverter()
    {
        DATE_TIME = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseStrict()
                
                // Date
                .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                
                // Time
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                
                // Optional Nanoseconds
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)

                // Optional time zone
                .optionalStart()
                .appendZoneRegionId()
                .optionalEnd()
                .toFormatter();
    }


    /**
     * Convert yyyyMMddHHmmss.SSSSS dates to ISO instant
     * @param value a date
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
