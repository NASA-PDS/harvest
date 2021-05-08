package gov.nasa.pds.harvest.util.date;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;


/**
 * Converts local dates (dates without time zone information)
 * to ISO instant. The following date formats are supported:
 * <ul>
 * <li>yyyy</li>
 * <li>yyyy-MM</li>
 * <li>yyyy-MM-dd</li>
 * <li>yyyy-DDD</li>
 * </ul>
 * 
 * @author karpenko
 *
 */
public class LocalDateConverter
{
    private SimpleDateFormat DATE_yyyy;
    private SimpleDateFormat DATE_yyyy_MM;
    private SimpleDateFormat DATE_yyyy_MM_dd;
    private SimpleDateFormat DATE_yyyy_DDD;

    
    /**
     * Constructor
     */
    public LocalDateConverter()
    {
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        
        DATE_yyyy = new SimpleDateFormat("yyyy");
        DATE_yyyy.setTimeZone(UTC);
        
        DATE_yyyy_MM = new SimpleDateFormat("yyyy-MM");
        DATE_yyyy_MM.setTimeZone(UTC);
        DATE_yyyy_MM.setLenient(false);
        
        DATE_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        DATE_yyyy_MM_dd.setTimeZone(UTC);
        DATE_yyyy_MM_dd.setLenient(false);
        
        DATE_yyyy_DDD = new SimpleDateFormat("yyyy-DDD");
        DATE_yyyy_DDD.setTimeZone(UTC);
        DATE_yyyy_DDD.setLenient(false);
    }
    
    
    /**
     * Convert a local date to ISO instant
     * @param value
     * @return
     */
    public Instant toInstant(String value)
    {
        if(value == null) return null;        
        int len = value.length();

        try
        {
            Date date = null;
            switch(len)
            {
            case 4:
                date = DATE_yyyy.parse(value);
                break;
            case 7:
                date = DATE_yyyy_MM.parse(value);
                break;
            case 8:
                date = DATE_yyyy_DDD.parse(value);
                break;
            case 10:
                date = DATE_yyyy_MM_dd.parse(value);
                break;
            }

            return (date == null) ? null : date.toInstant();
        }
        catch(Exception ex)
        {
            return null;
        }
    }
}
