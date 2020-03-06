package gov.nasa.pds.harvest.log;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils
{
    public static void setupLogger(String verbosity, String filePath)
    {
        if(filePath == null)
        {
            File dir = new File("/tmp/harvest");
            dir.mkdirs();
            filePath = "/tmp/harvest/harvest.log";
        }
        
        Logger log = Logger.getLogger("");
        
        // Remove default handlers
        for(Handler handler : log.getHandlers()) 
        {
            log.removeHandler(handler);
        }

        Level level = getLogLevel(verbosity);
        
        // Add custom handlers
        addConsoleHandler(log, level);
        addFileHandler(log, level, filePath);
        
        File file = new File(filePath);
        log.info("Logging to " + file.getAbsolutePath());
    }


    private static Level getLogLevel(String verbosity)
    {
        switch(verbosity)
        {
        case "0": return Level.ALL;
        case "1": return Level.INFO;
        case "2": return Level.WARNING;
        case "3": return Level.SEVERE;
        }
        
        System.out.println("WARNING: Invalid log verbosity '" + verbosity + "'. Will use 1 (Info).");
        return Level.INFO;
    }
    
    
    private static void addConsoleHandler(Logger log, Level level)
    {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new HarvestFormatter());
        handler.setLevel(level);
        log.addHandler(handler);
    }
    
    
    private static void addFileHandler(Logger log, Level level, String filePath)
    {
        try
        {
            FileHandler fhandler = new FileHandler(filePath, false);
            fhandler.setFormatter(new HarvestFormatter());
            log.addHandler(fhandler);
        }
        catch(Exception ex)
        {
            System.out.println("WARNING: Could not create log file " + filePath);
        }
    }    
}
