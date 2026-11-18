package gov.nasa.pds.harvest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;


/**
 * Records the Harvest log messages of level WARN, ERROR and FATAL while a test runs.
 */
public class LogCapture implements AutoCloseable {
  private static final String LOGGER_NAME = "gov.nasa.pds.harvest";

  private final List<String> warnings = Collections.synchronizedList(new ArrayList<>());
  private final List<String> errors = Collections.synchronizedList(new ArrayList<>());
  private final LoggerContext context = (LoggerContext) LogManager.getContext(false);
  private final AbstractAppender appender;


  public LogCapture() {
    appender = new AbstractAppender("LogCapture", null, null, true, null) {
      @Override
      public void append(LogEvent event) {
        if (event.getLevel() == Level.WARN) warnings.add(event.getMessage().getFormattedMessage());
        else if (event.getLevel() == Level.ERROR || event.getLevel() == Level.FATAL) errors.add(event.getMessage().getFormattedMessage());
      }
    };
    appender.start();

    Configuration config = context.getConfiguration();
    LoggerConfig loggerConfig = new LoggerConfig(LOGGER_NAME, Level.WARN, true);
    loggerConfig.addAppender(appender, null, null);
    config.addLogger(LOGGER_NAME, loggerConfig);
    context.updateLoggers();
  }


  /** Messages logged at WARN level. */
  public List<String> warnings() {
    return new ArrayList<>(warnings);
  }


  /** Messages logged at ERROR or FATAL level. */
  public List<String> errors() {
    return new ArrayList<>(errors);
  }


  @Override
  public void close() {
    context.getConfiguration().removeLogger(LOGGER_NAME);
    context.updateLoggers();
    appender.stop();
  }
}
