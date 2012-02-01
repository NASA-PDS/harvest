// Copyright 2006-2010, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
package gov.nasa.pds.harvest.commandline.options;

import org.apache.commons.cli.Options;

/**
 * Class that holds the command-line option flags.
 *
 * @author mcayanan
 *
 */
public enum Flag {
  /** Flag to specify a configuration file to configure the tool behavior.
   */
  CONFIG("c", "config", "file", String.class, "Specify a policy "
      + "configuration file to set the tool behavior. (This flag is "
      + "required)"),

  /**
   * Flag to specify file patterns to look for when validating a target
   * directory.
   */
  REGEXP("e", "regexp", "patterns", String.class, true, "Specify file "
      + "patterns to look for when crawling a target directory. Each "
      + "pattern should be surrounded by quotes. (i.e. -e \"*.xml\")"),

  /**
   * Flag to specify a PDS3 directory to crawl for harvesting.
   */
  PDS3DIRECTORY("D", "pds3-directory", "dir", String.class, "Specify a "
      + "PDS3 directory to crawl."),

  /** Flag to display the help. */
  HELP("h", "help", "Display usage."),

  /** Flag for the keystore password. */
  KEYSTOREPASS("k", "keystore-pass", "pwd", String.class, "Specify the "
      + "keystore password associated with the given keystore file."),

  /** Flag to output the logging to a file. */
  LOG("l", "log-file", "file name", String.class,
      "Specify a log file name. Default is standard out."),

  /** Flag for the username password. */
  PASSWORD("p", "password", "pwd", String.class,
      "Specify the password associated with the username"),

  /** Flag for the username of an authorized user. */
  USERNAME("u", "username", "name", String.class,
      "Specify a username to login to the PDS security service."),

  /** Flag for the daemon port number to be used if running the tool
   *  continuously.
   */
  PORT("P", "port", "number", int.class, "Specify a port number to use "
      + "if running the tool in continous mode."),

  /** Flag to specify the wait time in between crawls. */
  WAIT("w", "wait", "seconds", int.class, "Specify the wait time in "
      + "seconds in between crawls if running in continuous mode."),

  /** Flag to display the version. */
  VERSION("V", "version", "Display application version.");

  /** The short name of the flag. */
  private final String shortName;

  /** The long name of the flag. */
  private final String longName;

  /** An argument name for the flag, if it accepts argument values. */
  private final String argName;

  /** The type of argument values the flag accepts. */
  private final Object argType;

  /** A boolean value indicating if the flag accepts more than one
   * argument.
   */
  private final boolean allowsMultipleArgs;

  /** The flag description. */
  private final String description;

  /** A list of Option objects for command-line processing. */
  private static Options options;

  /**
   * Constructor.
   *
   * @param shortName The short name.
   * @param longName The long name.
   * @param description A description of the flag.
   */
  private Flag(final String shortName, final String longName,
      final String description) {
    this(shortName, longName, null, null, description);
  }

  /**
   * Constructor for flags that can take arguments.
   *
   * @param shortName The short name.
   * @param longName The long name.
   * @param argName The argument name.
   * @param argType The argument type.
   * @param description A description of the flag.
   */
  private Flag(final String shortName, final String longName,
      final String argName, final Object argType,
      final String description) {
    this(shortName, longName, argName, argType, false, description);
  }

  /**
   * Constructor for flags that can take arguments.
   *
   * @param shortName The short name.
   * @param longName The long name.
   * @param argName The argument name.
   * @param argType The argument type.
   * @param description A description of the flag.
   */
  private Flag(final String shortName, final String longName,
      final String argName, final Object argType,
      final boolean allowsMultipleArgs, final String description) {
    this.shortName = shortName;
    this.longName = longName;
    this.argName = argName;
    this.argType = argType;
    this.allowsMultipleArgs = allowsMultipleArgs;
    this.description = description;
  }

  /**
   * Get the short name of the flag.
   *
   * @return The short name.
   */
  public String getShortName() {
    return shortName;
  }

  /**
   * Get the long name of the flag.
   *
   * @return The long name.
   */
  public String getLongName() {
    return longName;
  }

  /**
   * Get the argument name of the flag.
   *
   * @return The argument name.
   */
  public String getArgName() {
    return argName;
  }

  /**
   * Find out if the flag can handle multiple arguments.
   *
   * @return 'true' if yes.
   */
  public boolean allowsMultipleArgs() {
    return allowsMultipleArgs;
  }

  /**
   * Get the argument type of the flag.
   *
   * @return The argument type.
   */
  public Object getArgType() {
    return argType;
  }

  /**
   * Get the flag description.
   *
   * @return The description.
   */
  public String getDescription() {
    return description;
  }

  static {
    options = new Options();

    options.addOption(new ToolsOption(CONFIG));
    options.addOption(new ToolsOption(REGEXP));
    options.addOption(new ToolsOption(PDS3DIRECTORY));
    options.addOption(new ToolsOption(HELP));
    options.addOption(new ToolsOption(KEYSTOREPASS));
    options.addOption(new ToolsOption(VERSION));
    options.addOption(new ToolsOption(PASSWORD));
    options.addOption(new ToolsOption(USERNAME));
    options.addOption(new ToolsOption(LOG));
    options.addOption(new ToolsOption(PORT));
    options.addOption(new ToolsOption(WAIT));
  }

  /**
   * Get the command-line options.
   *
   * @return A class representation of the command-line options.
   */
  public static Options getOptions() {
    return options;
  }
}
