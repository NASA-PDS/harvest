package gov.nasa.pds.harvest.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class HarvestFormatter extends Formatter
{
    private Date date = new Date();
    private String format = "%1$tF %1$tT [%2$s] %3$s %4$s %n";;

    
    public HarvestFormatter()
    {
    }

    
    @Override
    public String format(LogRecord record)
    {
        date.setTime(record.getMillis());
        
        String message = formatMessage(record);
        
        String throwable = "";        
        if(record.getThrown() != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        
        return String.format(format, 
                date, 
                record.getLevel().getName(), 
                message, 
                throwable);
    }

}
