// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// * Neither the name of Caltech nor its operating division, the Jet Propulsion
// Laboratory, nor the names of its contributors may be used to endorse or
// promote products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package gov.nasa.pds.harvest.search.commandline.options;

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
  CONFIG("c", "harvest-config", "file", String.class, "Specify the harvest policy "
      + "configuration file to set the tool behavior. (This flag is "
      + "required)"),

  DOC_CONFIG("C", "doc-config", "dir", String.class, "Specify the directory "
      + "location where the document generation configuration files reside. "
      + "The default is to look in the 'search-conf' directory that resides in the "
      + "tool package"),
  
  /**
   * Flag to specify file patterns to look for when validating a target
   * directory.
   */
  REGEXP("e", "regexp", "patterns", String.class, true, "Specify file "
      + "patterns to look for when crawling a target directory. Each "
      + "pattern should be surrounded by quotes. (i.e. -e \"*.xml\")"),

  /**
   * Flag to specify patterns to look for when crawling a target directory
   * for sub-directories to ignore.
   *
   */
  IGNOREDIR("D", "ignore-dir", "patterns", String.class, true, "Specify "
      + "patterns to look for when crawling a target directory for "
      + "sub-directories to ignore. Each pattern should be surrounded by "
      + "quotes. (i.e. -i \"CATALOG\")"),

  /**
   * Flag to specify a PDS3 directory to crawl for harvesting.
   */
  ISPDS3DIR("pds3", "is-pds3-dir", "Specify this flag to indicate that the "
      + "target passed into the command-line is a PDS3 directory. The "
      + "default assumes that any targets passed into the command line are "
      + "PDS4 directories."),

  /** Flag to display the help. */
  HELP("h", "help", "Display usage."),

  /** Flag to output the logging to a file. */
  LOG("l", "log-file", "file name", String.class,
      "Specify a log file name. Default is standard out."),

  OUTPUT_DIR("o", "output-dir", "dir", String.class,
      "Specify a directory location to tell the tool where to output the "
       + "Solr documents. The default is to write to the current working directory."),
  
  /** Flag for the daemon port number to be used if running the tool
   *  continuously.
   */
  PORT("P", "port", "number", int.class, "Specify a port number to use "
      + "if running the tool in persistance mode."),

  /** Flag to specify the wait time in between crawls. */
  WAIT("w", "wait", "seconds", int.class, "Specify the wait time in "
      + "seconds in between crawls if running in persistance mode."),

  /** Flag to display the version. */
  VERSION("V", "version", "Display application version."),

  /** Flag to change the severity level of the messaging in the report. */
  VERBOSE("v", "verbose", "level", int.class, "Specify the severity level "
      + "and above to include in the log: "
      + "(0=Debug, 1=Info, 2=Warning, 3=Error). "
      + "Default is Info and above (level 1).");

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
    options.addOption(new ToolsOption(DOC_CONFIG));
    options.addOption(new ToolsOption(REGEXP));
    options.addOption(new ToolsOption(IGNOREDIR));
    options.addOption(new ToolsOption(ISPDS3DIR));
    options.addOption(new ToolsOption(HELP));
    options.addOption(new ToolsOption(OUTPUT_DIR));
    options.addOption(new ToolsOption(VERBOSE));
    options.addOption(new ToolsOption(VERSION));
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
