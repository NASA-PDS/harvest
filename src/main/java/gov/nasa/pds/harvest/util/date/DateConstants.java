package gov.nasa.pds.harvest.util.date;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Some constant values used by date parsers. 
 * 
 * @author karpenko
 */
public class DateConstants
{
    public static final DateTimeFormatter OPTIONAL_TIME = new DateTimeFormatterBuilder()
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
