// Copyright 2006-2019, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.search;

import gov.nasa.pds.harvest.search.commandline.options.Flag;
import gov.nasa.pds.harvest.search.commandline.options.InvalidOptionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.logging.formatter.HarvestFormatter;
import gov.nasa.pds.harvest.search.logging.handler.HarvestFileHandler;
import gov.nasa.pds.harvest.search.logging.handler.HarvestStreamHandler;
import gov.nasa.pds.harvest.search.policy.Directory;
import gov.nasa.pds.harvest.search.policy.DirectoryFilter;
import gov.nasa.pds.harvest.search.policy.FileFilter;
import gov.nasa.pds.harvest.search.policy.Manifest;
import gov.nasa.pds.harvest.search.policy.Namespace;
import gov.nasa.pds.harvest.search.policy.Pds3Directory;
import gov.nasa.pds.harvest.search.policy.Policy;
import gov.nasa.pds.harvest.search.policy.PolicyReader;
import gov.nasa.pds.harvest.search.target.TargetType;
import gov.nasa.pds.harvest.search.util.PDSNamespaceContext;
import gov.nasa.pds.harvest.search.util.ToolInfo;
import gov.nasa.pds.harvest.search.util.Utility;
import gov.nasa.pds.harvest.search.util.XMLExtractor;
import gov.nasa.pds.search.core.exception.SearchCoreException;
import gov.nasa.pds.search.core.exception.SearchCoreFatalException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.trans.XPathException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;

/**
 * Wrapper class of the Harvest tool that handles the command-line processing.
 *
 * @author mcayanan
 *
 */
public class HarvestSearchLauncher {
  /** logger object. */
  private static Logger log = Logger.getLogger(
      HarvestSearchLauncher.class.getName());

  /** Policy file. */
  private File policy;

  /** The internal policy file. */
  private URL globalPolicy;

  /** A log file name. */
  private String logFile;

  /** The wait interval in seconds in between crawls if running
   *  the tool in continuous mode. */
  private int waitInterval;

  /** The port number to use for the daemon if running the tool
   *  in continuous mode.
   */
  private int daemonPort;

  /** List of targets specified on the command line. */
  private List<File> targets;

  /** List of regular expressions to use as file filters when crawling a
   *  directory.
   */
  private List<String> regExps;

  /**
   * List of regular expressions for sub-directories to exclude when crawling
   * a directory.
   */
  private List<String> excludeSubDirs;

  /** Indicates whether the target is a PDS3 directory. */
  private boolean isPDS3Directory;

  /** The severity level to set for the tool. */
  private Level severityLevel;
  
  private File configDir;
  
  private File outputDir;
  
  private File registeredResources;

  private String searchUrl;
  
  /**
   * Default constructor.
   *
   */
  public HarvestSearchLauncher() {
    policy = null;
    logFile = null;
    waitInterval = -1;
    daemonPort = -1;
    targets = new ArrayList<File>();
    regExps = new ArrayList<String>();
    excludeSubDirs = new ArrayList<String>();
    isPDS3Directory = false;
    severityLevel = ToolsLevel.INFO;
    outputDir = null;
    configDir = null;
    String value = System.getProperty("pds.harvest.search.conf");
    if (value != null && !value.isEmpty()) {
      configDir = new File(value);
    }

    globalPolicy = this.getClass().getResource("global-policy.xml");
    searchUrl = System.getProperty("pds.search");
    registeredResources = new File(System.getProperty("resources.home")
        + File.separator + "registered_resources.json");
  }

  /**
   * A method to parse the command-line arguments.
   *
   * @param args The command-line arguments
   * @return A class representation of the command-line arguments
   *
   * @throws ParseException If there was an error during parsing.
   */
  public final CommandLine parse(final String[] args)
  throws ParseException {
    CommandLineParser parser = new GnuParser();
    return parser.parse(Flag.getOptions(), args);
  }

  /**
   * Examines the command-line arguments passed into the Harvest Tool
   * and takes the appropriate action based on what flags were set.
   *
   * @param line A class representation of the command-line arguments.
   *
   * @throws Exception If there was an error while querying the options
   * that were set on the command-line.
   */
  public final void query(final CommandLine line) throws Exception {
    if (searchUrl == null) {
      throw new Exception("\'pds.search\' java property is not set.");
    }
    List<String> targetList = new ArrayList<String>();
    for (Iterator<String> i = line.getArgList().iterator(); i.hasNext();) {
      String[] values = i.next().split(",");
      for (int index = 0; index < values.length; index++) {
        targetList.add(values[index].trim());
      }
    }
    if (!targetList.isEmpty()) {
      setTargets(targetList);
    }
    List<Option> processedOptions = Arrays.asList(line.getOptions());
    for (Option o : processedOptions) {
      if (o.getOpt().equals(Flag.HELP.getShortName())) {
        displayHelp();
        System.exit(0);
      } else if (o.getOpt().equals(Flag.VERSION.getShortName())) {
        displayVersion();
        System.exit(0);
      } else if (o.getOpt().equals(Flag.CONFIG.getShortName())) {
        policy = new File(o.getValue());
        if (!policy.exists()) {
          throw new InvalidOptionException("Policy file does not exist: "
              + policy);
         }
      } else if (o.getOpt().equals(Flag.REGEXP.getShortName())) {
        setRegExps((List<String>) o.getValuesList());
      } else if (o.getOpt().equals(Flag.LOG.getShortName())) {
        logFile = o.getValue();
      } else if (o.getOpt().equals(Flag.PORT.getShortName())) {
        try {
          daemonPort = Integer.parseInt(o.getValue());
        } catch (NumberFormatException n) {
          throw new Exception(n.getMessage());
        }
      } else if (o.getOpt().equals(Flag.WAIT.getShortName())) {
        try {
          waitInterval = Integer.parseInt(o.getValue());
        } catch (NumberFormatException n) {
          throw new Exception(n.getMessage());
        }
      } else if (o.getOpt().equals(Flag.ISPDS3DIR.getShortName())) {
        isPDS3Directory = true;
      } else if (o.getOpt().equals(Flag.VERBOSE.getShortName())) {
        setVerbose(Integer.parseInt(o.getValue()));
      } else if (o.getOpt().equals(Flag.DOC_CONFIG.getShortName())) {
        configDir = new File(o.getValue());
      } else if (o.getOpt().equals(Flag.OUTPUT_DIR.getShortName())) {
        outputDir = new File(o.getValue(), Constants.SOLR_DOC_DIR);
      }
    }
    if (policy == null) {
      throw new Exception("Missing '-c' flag option. Policy file must be "
          + "specified.");
    }
    if (isPDS3Directory) {
      if (targets.size() == 0) {
        throw new Exception("No targets specified on the command-line.");
      } else if (targets.size() > 1) {
        throw new Exception("Cannot specify more than one PDS3 target "
            +"directory.");
      }
    }
    if (outputDir == null) {
    	String dataPath = System.getProperty("pds.registry.data");
    	if (dataPath != null) {
    		outputDir = new File(dataPath);
	    } else {
	    	outputDir = new File("").getAbsoluteFile();
	    }
    }
    
    setLogger();
  }

  /**
   * Set the target.
   *
   * @param targets A list of targets.
   */
  private void setTargets(List<String> targets) {
    this.targets.clear();
    while (targets.remove(""));
    for (String t : targets) {
      this.targets.add(new File(t));
    }
  }

  /**
   * Sets the list of file patterns to look for if traversing a directory.
   *
   * @param patterns A list of file patterns.
   */
  private void setRegExps(List<String> patterns) {
    this.regExps = patterns;
    while (this.regExps.remove(""));
  }

  /**
   * Set the verbosity level and above to include in the reporting.
   * @param v '1' for info, '2' for warnings, and '3' for errors
   * @throws ApplicationException
   */
  private void setVerbose(int v) throws Exception {
    if (v < 0 || v > 3) {
      throw new Exception("Invalid value entered for 'v' flag. "
          + "Valid values can only be 0, 1, 2, or 3");
    }
    if (v == 0) {
      severityLevel = ToolsLevel.DEBUG;
    } else if (v == 1) {
      severityLevel = ToolsLevel.INFO;
    } else if (v == 2) {
      severityLevel = ToolsLevel.WARNING;
    } else if (v == 3) {
      severityLevel = ToolsLevel.SEVERE;
    }
  }

  /**
   * Logs header information for the log output.
   *
   */
  private void logHeader(Policy policy) {
    List<String> targets = new ArrayList<String>();
    List<String> fileIncludes = new ArrayList<String>();
    List<String> fileExcludes = new ArrayList<String>();
    List<String> dirExcludes = new ArrayList<String>();
    if (!policy.getPds3Directories().getPath().isEmpty()) {
      targets.addAll(policy.getPds3Directories().getPath());
      fileIncludes = policy.getPds3Directories().getFileFilter().getInclude();
      fileExcludes = policy.getPds3Directories().getFileFilter().getExclude();
      dirExcludes = policy.getPds3Directories().getDirectoryFilter().getExclude();
    } else {
      targets = policy.getDirectories().getPath();
      fileIncludes = policy.getDirectories().getFileFilter().getInclude();
      fileExcludes = policy.getDirectories().getFileFilter().getExclude();
      dirExcludes = policy.getDirectories().getDirectoryFilter().getExclude();
    }
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "PDS Harvest Tool Log\n"));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Version                     " + ToolInfo.getVersion()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Time                        " + Utility.getDateTime()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Target(s)                   " + targets));
    if (!policy.getPds3Directories().getPath().isEmpty()) {
      String objectType = policy.getCandidates().getPds3ProductMetadata()
          .getObjectType();
      if (objectType != null && objectType.equals(Constants.FILE_OBJECT_PRODUCT_TYPE)) {
        log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
            "Target Type                 PDS3 Files Only"));
      }  else {
        log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
            "Target Type                 PDS3"));
      }
    }
    if (!fileIncludes.isEmpty()) {
      log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "File Inclusions             " + fileIncludes));
    }
    if (!fileExcludes.isEmpty()) {
      log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "File Exclusions             " + fileExcludes));
    }
    if (!dirExcludes.isEmpty()) {
      log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Directory Exclusions        " + dirExcludes));
    }
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Severity Level              " + severityLevel.getName()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Config directory            " + configDir.toString()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Output directory            " + outputDir.toString() + "\n"));    
  }

  /**
   * Sets the appropriate handlers for the logging.
   *
   * @throws IOException If a log file was specified and could not
   * be read.
   */
  private void setLogger() throws IOException {
    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.ALL);
    Handler []handler = logger.getHandlers();
    for (int i = 0; i < logger.getHandlers().length; i++) {
      logger.removeHandler(handler[i]);
    }
    if (logFile != null) {
      logger.addHandler(new HarvestFileHandler(logFile, severityLevel,
          new HarvestFormatter()));
    } else {
      logger.addHandler(new HarvestStreamHandler(System.out,
          severityLevel, new HarvestFormatter()));
    }
  }

  /**
   * Displays the current version and disclaimer notice.
   *
   */
  public final void displayVersion() {
    System.err.println("\n" + ToolInfo.getName());
    System.err.println(ToolInfo.getVersion());
    System.err.println("Release Date: " + ToolInfo.getReleaseDate());
    System.err.println(ToolInfo.getCopyright() + "\n");
  }

  /**
   * Displays tool usage.
   *
   */
  public final void displayHelp() {
    int maxWidth = 80;
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(maxWidth, "harvest <options>",
        null, Flag.getOptions(), null);
  }

  /**
   * Closes the handlers for the logger.
   *
   */
  private void closeHandlers() {
    Logger logger = Logger.getLogger("");
    Handler []handlers = logger.getHandlers();
    for (int i = 0; i < logger.getHandlers().length; i++) {
      handlers[i].close();
    }
  }

  /**
   * Perform harvesting of the target files.
   *
   * @param policy Class representation of the policy file.
   *
   * @throws ParserConfigurationException If an error occurred during
   * metadata extraction.
   * @throws RegistryClientException If an error occurred while setting
   * up the Harvester with the PDS Security Service.
   * @throws ConnectionException
   * @throws IOException
   * @throws SearchCoreFatalException 
   * @throws SearchCoreException 
   */
  private void doHarvesting(final Policy policy)
  throws ParserConfigurationException, IOException, ConnectionException, 
  SearchCoreException, SearchCoreFatalException {
    HarvesterSearch harvester = new HarvesterSearch(this.searchUrl, 
        this.configDir, this.outputDir, this.registeredResources);
    if (daemonPort != -1 && waitInterval != -1) {
      harvester.setDaemonPort(daemonPort);
      harvester.setWaitInterval(waitInterval);
    }
    Directory directories = new Directory();
    Pds3Directory pds3Dir = new Pds3Directory();
    FileFilter fileFilter = new FileFilter();
    fileFilter.getInclude().addAll(regExps);

    DirectoryFilter dirFilter = new DirectoryFilter();
    dirFilter.getExclude().addAll(excludeSubDirs);

    if (isPDS3Directory) {
      for (File target : targets) {
        pds3Dir.getPath().add(target.toString());
      }
      pds3Dir.setFileFilter(fileFilter);
      pds3Dir.setDirectoryFilter(dirFilter);
    } else {
      for (File target : targets) {
        directories.getPath().add(target.toString());
      }
      directories.setFileFilter(fileFilter);
      directories.setDirectoryFilter(dirFilter);
    }
    // Any targets specified on the command line will overwrite any targets
    // specified in the policy file.
    if ( (!directories.getPath().isEmpty())
        || (!pds3Dir.getPath().isEmpty()) ) {
      policy.setDirectories(directories);
      policy.setPds3Directories(pds3Dir);
    }
    // Set the base path if a checksum manifest file was specified
    if (policy.getChecksums().getManifest() != null) {
      Manifest m = policy.getChecksums().getManifest();
      if (m.getBasePath() == null) {
        if (policy.getDirectories().getPath().size() > 1 ||
            policy.getPds3Directories().getPath().size() > 1) {
          throw new ParserConfigurationException("Must specify a basePath "
              + "for the Checksum Manifest file if multiple target "
              + "directories are specified.");
        }
        String basePath = "";
        if (!policy.getDirectories().getPath().isEmpty()) {
          basePath = policy.getDirectories().getPath().get(0);
        } else if (!policy.getPds3Directories().getPath().isEmpty()) {
          basePath = policy.getPds3Directories().getPath().get(0);
        } else {
          throw new ParserConfigurationException("No target directory "
              + "specified in the configuration to set the base path "
              + "for the Checksum Manifest file.");
        }
        log.log(new ToolsLogRecord(ToolsLevel.DEBUG, 
            "Setting base path for use in the Checksum Manifest file to '"
                + basePath + "'"));
        m.setBasePath(basePath);
      }
    }
    // Display config parameters in the report log
    logHeader(policy);
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "XML extractor set to the "
        + "following default namespace: "
        + XMLExtractor.getDefaultNamespace()));
    harvester.harvest(policy);
  }

  /**
   * Sets up the configuration for the XML extractor.
   *
   * @param namespaces A list of namespaces.
   *
   */
  private void setupExtractor(List<Namespace> namespaces) {
    String defaultNamespaceUri = "";
    for (Iterator<Namespace> i = namespaces.iterator();
      i.hasNext();) {
      Namespace n = i.next();
      if (n.isDefault()) {
        defaultNamespaceUri = n.getUri();
        break;
      }
    }
    // isEmpty() is a java 1.6 method
    if (!defaultNamespaceUri.equals("")) {
//  if (!defaultNamespaceUri.isEmpty()) {
      XMLExtractor.setDefaultNamespace(defaultNamespaceUri);
    }
    if (!namespaces.isEmpty()) {
      XMLExtractor.setNamespaceContext(new PDSNamespaceContext(
          namespaces));
    }
  }

  /**
   * Get the target type of the file.
   *
   * @param target The file.
   *
   * @return A TargetType. The default is a file if the product_class
   * tag value is not part of the list of bundle and collection type
   * names.
   *
   * @throws XPathException If an error occurred while parsing the file.
   */
  private TargetType getTargetType(File target) throws XPathException,
  FileNotFoundException {
    String PRODUCT_TYPE_XPATH = Constants.IDENTIFICATION_AREA_XPATH + "/"
    + Constants.OBJECT_TYPE;
    TargetType type = TargetType.FILE;
    if (!target.exists()) {
      throw new FileNotFoundException("Target does not exist: " + target);
    } else if (target.isDirectory()) {
      type = TargetType.DIRECTORY;
    } else {
      XMLExtractor extractor = new XMLExtractor();
      extractor.parse(target);
      String value = "";
      try {
        value = extractor.getValueFromDoc(PRODUCT_TYPE_XPATH);
      } catch (XPathExpressionException x) {
        throw new XPathException("Bad xpath expression: "
            + PRODUCT_TYPE_XPATH);
      }
      if (value.contains("Bundle")) {
        type = TargetType.BUNDLE;
      } else if (value.contains("Collection")) {
        type = TargetType.COLLECTION;
      }
    }
    return type;
  }

  private void backupOutputDirectory(File outputDir) throws IOException {
    if (outputDir.isDirectory()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
      String now = dateFormat.format(new Date());
      File backup = new File(outputDir.getParent(), Constants.SOLR_DOC_DIR + "_pre" + now);
      if (backup.isDirectory()) {
        FileUtils.deleteQuietly(backup);
      }
      FileUtils.moveDirectory(outputDir, backup);
      FileUtils.forceMkdir(outputDir);
    }
  }
  
  /**
   * Process main.
   *
   * @param args Command-line arguments.
   */
  private void processMain(String []args) {
    //This removes the log4j warnings
    ConsoleAppender ca = new ConsoleAppender(new PatternLayout("%-5p %m%n"));
    ca.setThreshold(Priority.FATAL);
    BasicConfigurator.configure(ca);
    if (args.length == 0) {
      System.out.println("\nType 'harvest -h' for usage");
      System.exit(0);
    }
    try {
      CommandLine commandline = parse(args);
      query(commandline);
      Policy policy = PolicyReader.unmarshall(this.policy);
      Policy globalPolicy = PolicyReader.unmarshall(this.globalPolicy);
      policy.getCandidates().getNamespace().addAll(
          globalPolicy.getCandidates().getNamespace());
      policy.getCandidates().getProductMetadata().addAll(
          globalPolicy.getCandidates().getProductMetadata());
      policy.getReferences().getReferenceTypeMap().addAll(
          globalPolicy.getReferences().getReferenceTypeMap());
      policy.getFileTypes().getFileTypeMap().addAll(
          globalPolicy.getFileTypes().getFileTypeMap());
      setupExtractor(policy.getCandidates().getNamespace());
      backupOutputDirectory(outputDir);
      doHarvesting(policy);
    } catch (JAXBException je) {
      //Don't do anything
    } catch (ParseException pEx) {
      System.err.println("Command-line parse failure: "
            + pEx.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    } finally {
      closeHandlers();
    }
  }

  /**
   * Main class.
   *
   * @param args The command-line arguments.
   */
  public static void main(final String []args) {
    new HarvestSearchLauncher().processMain(args);
  }
}
