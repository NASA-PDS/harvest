package gov.nasa.pds.harvest.util.solr;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;


public class PDSDateConverter
{
    public static final String DEFAULT_STARTTIME = "1965-01-01T00:00:00.000Z";
    public static final String DEFAULT_STOPTIME = "3000-01-01T00:00:00.000Z";

   
    public static final DateTimeFormatter LOCAL_DOY;
    static 
    {
        LOCAL_DOY = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_YEAR, 3)
            .parseStrict()
            .toFormatter();
    }

    
    public static final DateTimeFormatter LOCAL_TIME;
    static 
    {
        LOCAL_TIME = new DateTimeFormatterBuilder()
            // Required Hours
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            // Optional Minutes
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            // Optional Seconds
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            // Optional Nanoseconds
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter();
    }

    
    public static final DateTimeFormatter DATE_TIME;
    static 
    {
        DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()            
            // Date
            .append(DateTimeFormatter.ISO_LOCAL_DATE)            
            // Time
            .appendLiteral('T')
            .append(LOCAL_TIME)            
            // Optional time zone
            .optionalStart()
            .appendZoneRegionId()
            .optionalEnd()            
            .toFormatter();
    }

    
    public static final DateTimeFormatter DATE_TIME_2;
    static 
    {
        DATE_TIME_2 = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()            
            // Date
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
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

    
    public static final DateTimeFormatter DOY_TIME;
    static 
    {
        DOY_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            // Date
            .append(LOCAL_DOY)
            // Time
            .appendLiteral('T')
            .append(LOCAL_TIME)
            // Optional time zone
            .optionalStart()
            .appendZoneRegionId()
            .optionalEnd()
            .toFormatter();
    }

    
    public PDSDateConverter()
    {
    }


    public static String toSolrDateString(String fieldName, String value)
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
            //if(isValidIsoInstant(value)) return value;
            
            String newValue = convertDateTime(value);
            if(newValue != null) return newValue;

            newValue = convertDateTime2(value);
            if(newValue != null) return newValue;

            newValue = convertDoyTime(value);
            if(newValue != null) return newValue;
        }
        // Date only
        else
        {
            
        }
        
        return null;
    }


    private static String convertDateTime(String value)
    {
        try
        {
            TemporalAccessor tmp = PDSDateConverter.DATE_TIME.parseBest(value, ZonedDateTime::from, LocalDateTime::from);
            return toInstantString(tmp);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private static String convertDateTime2(String value)
    {
        try
        {
            TemporalAccessor tmp = PDSDateConverter.DATE_TIME_2.parseBest(value, ZonedDateTime::from, LocalDateTime::from);
            return toInstantString(tmp);
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    private static String convertDoyTime(String value)
    {
        try
        {
            TemporalAccessor tmp = PDSDateConverter.DOY_TIME.parseBest(value, ZonedDateTime::from, LocalDateTime::from);
            return toInstantString(tmp);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    
    private static String toInstantString(TemporalAccessor tmp)
    {
        if(tmp instanceof ZonedDateTime)
        {
            return DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.from(tmp).toInstant());
        }
        else if(tmp instanceof LocalDateTime)
        {
            return DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.from(tmp).toInstant(ZoneOffset.UTC));
        }

        return null;
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
