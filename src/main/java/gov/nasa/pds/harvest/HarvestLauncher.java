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
package gov.nasa.pds.harvest;

import gov.nasa.pds.harvest.commandline.options.Flag;
import gov.nasa.pds.harvest.commandline.options.InvalidOptionException;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.logging.formatter.HarvestFormatter;
import gov.nasa.pds.harvest.logging.handler.HarvestFileHandler;
import gov.nasa.pds.harvest.logging.handler.HarvestStreamHandler;
import gov.nasa.pds.harvest.policy.Directory;
import gov.nasa.pds.harvest.policy.Namespace;
import gov.nasa.pds.harvest.policy.Pds3Directory;
import gov.nasa.pds.harvest.policy.Policy;
import gov.nasa.pds.harvest.policy.PolicyReader;
import gov.nasa.pds.harvest.target.TargetType;
import gov.nasa.pds.harvest.util.PDSNamespaceContext;
import gov.nasa.pds.harvest.util.ToolInfo;
import gov.nasa.pds.harvest.util.Utility;
import gov.nasa.pds.harvest.util.XMLExtractor;
import gov.nasa.pds.registry.client.RegistryClient;
import gov.nasa.pds.registry.client.SecurityContext;
import gov.nasa.pds.registry.exception.RegistryClientException;
import gov.nasa.pds.registry.exception.RegistryServiceException;
import gov.nasa.pds.registry.model.RegistryPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
public class HarvestLauncher {
  /** logger object. */
  private static Logger log = Logger.getLogger(
      HarvestLauncher.class.getName());

  /** Policy file. */
  private File policy;

  /** The internal policy file. */
  private InputStream globalPolicy;

  /** password of the authorized user. */
  private String password;

  /** an authorized user. */
  private String username;

  /** URL of the registry service. */
  private String registryURL;

  /** Path to the self sign on keystore. */
  private String keystore;

  /** A log file name. */
  private String logFile;

  /** The wait interval in seconds in between crawls if running
   *  the tool in continuous mode. */
  private int waitInterval;

  /** The port number to use for the daemon if running the tool
   *  in continuous mode.
   */
  private int daemonPort;

  /** Security context to support handling of the PDS Security. */
  private SecurityContext securityContext;

  /** Keystore password for the self sign certificate. */
  private String keystorePassword;

  /** The GUID of the registry package created before harvesting of products.
   */
  private String registryPackageGuid;

  /** The registry package name to be used when harvesting products. */
  private String registryPackageName;

  /** List of targets specified on the command line. */
  private List<File> targets;

  /** List of regular expressions to use as filters when crawling a directory.
   */
  private List<String> regExps;

  /** A PDS3 directory specified on the command line. */
  private String pds3Directory;

  /** The severity level to set for the tool. */
  private Level severityLevel;

  /**
   * Default constructor.
   *
   */
  public HarvestLauncher() {
    policy = null;
    password = null;
    username = null;
    registryURL = null;
    keystore = null;
    logFile = null;
    waitInterval = -1;
    daemonPort = -1;
    securityContext = null;
    registryPackageGuid = null;
    registryPackageName = null;
    keystorePassword = null;
    targets = new ArrayList<File>();
    regExps = new ArrayList<String>();
    pds3Directory = null;
    severityLevel = ToolsLevel.INFO;

    globalPolicy = this.getClass().getResourceAsStream("global-policy.xml");
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
    registryURL = System.getProperty("pds.registry");
    keystore = System.getProperty("pds.security.keystore");
    if (registryURL == null) {
      throw new Exception("\'pds.registry\' java property is not set.");
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
      } else if (o.getOpt().equals(Flag.KEYSTOREPASS.getShortName())) {
        keystorePassword = o.getValue();
      } else if (o.getOpt().equals(Flag.PASSWORD.getShortName())) {
        password = o.getValue();
      } else if (o.getOpt().equals(Flag.USERNAME.getShortName())) {
        username = o.getValue();
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
      } else if (o.getOpt().equals(Flag.PDS3DIRECTORY.getShortName())) {
        pds3Directory = o.getValue();
      } else if (o.getOpt().equals(Flag.VERBOSE.getShortName())) {
        setVerbose(Integer.parseInt(o.getValue()));
      }
    }
    if (policy == null) {
      throw new Exception("Missing '-c' flag option. Policy file must be "
          + "specified.");
    }
    if ((username != null && password == null)
        || (username == null && password != null)) {
      throw new InvalidOptionException(
        "Username and/or password must be specified.");
    }
    if ( (username != null) && (password != null) ) {
      if (keystore == null) {
        throw new Exception("\'pds.security.keystore\' java property not "
            + "set.");
      } else if (keystorePassword == null) {
        throw new Exception("Keystore password must be specified.");
      }
    }
    if (keystore != null) {
      if (!new File(keystore).exists()) {
        throw new Exception("Keystore file does not exist: " + keystore);
      }
      securityContext = new SecurityContext(keystore, keystorePassword,
          keystore, keystorePassword);
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
  private void logHeader() {
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "PDS Harvest Tool Log\n"));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Version                     " + ToolInfo.getVersion()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Time                        " + Utility.getDateTime()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Severity Level              " + severityLevel.getName()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Registry Location           " + registryURL.toString()));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Registry Package Name       " + registryPackageName));
    log.log(new ToolsLogRecord(ToolsLevel.CONFIGURATION,
        "Registration Package GUID   " + registryPackageGuid + "\n"));
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
   * @throws MalformedURLException If the URL to the registry service
   * is invalid.
   * @throws RegistryClientException If an error occurred while setting
   * up the Harvester with the PDS Security Service.
   * @throws ConnectionException
   */
  private void doHarvesting(final Policy policy)
  throws MalformedURLException, ParserConfigurationException,
  RegistryClientException, ConnectionException {
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "XML extractor set to the "
        + "following default namespace: "
        + XMLExtractor.getDefaultNamespace()));
    Harvester harvester = new Harvester(registryURL, registryPackageGuid);
    if ((username != null) && (password != null)) {
      harvester.setSecurity(securityContext, username, password);
    }
    if (daemonPort != -1 && waitInterval != -1) {
      harvester.setDaemonPort(daemonPort);
      harvester.setWaitInterval(waitInterval);
    }
    Directory directories = new Directory();
    Pds3Directory pds3Dir = new Pds3Directory();
    for (File target : targets) {
      directories.getPath().add(target.toString());
      directories.getFilePattern().addAll(regExps);
    }
    if (pds3Directory != null) {
      pds3Dir.setPath(pds3Directory);
      pds3Dir.getFilePattern().addAll(regExps);
    }
    // Any targets specified on the command line will overwrite any targets
    // specified in the policy file.
    if ( (!directories.getPath().isEmpty())
        || (pds3Dir.getPath() != null) ) {
      policy.setDirectories(directories);
      policy.setPds3Directory(pds3Dir);
    }
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
   * Creates a registry package to be used to associate all products
   * being registered during a single Harvest run.
   *
   * @param name Package name. Default is to give the name
   * 'Harvest-Package_\<datetimestamp\>'.
   *
   * @param description Pacakge description.
   *
   * @throws RegistryClientException If an error occurred while initializing
   * the RegistryClient.
   * @throws RegistryServiceException If an error occurred while trying to
   * register a package to the Registry.
   */
  private void createRegistryPackage(Policy policy)
  throws RegistryClientException, RegistryServiceException {
    RegistryClient client = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    if ( (username != null) && (password != null) ) {
      client = new RegistryClient(registryURL, securityContext, username,
          password);
    } else {
      client = new RegistryClient(registryURL);
    }
    if (policy.getRegistryPackage().getName() != null) {
      registryPackageName = policy.getRegistryPackage().getName();
    } else {
      registryPackageName = "Harvest-Package_" + dateFormat.format(
          new Date().getTime());
    }
    RegistryPackage registryPackage = new RegistryPackage();
    registryPackage.setName(registryPackageName);
    if (policy.getRegistryPackage().getDescription() != null) {
      registryPackage.setDescription(
          policy.getRegistryPackage().getDescription());
    } else {
      List<String> targets = new ArrayList<String>();
      for (File f : this.targets) {
        targets.add(f.toString());
      }
      if (pds3Directory != null) {
        targets.add(pds3Directory);
      }
      // Any targets specified on the command-line overwrite targets
      // specified in the policy config file.
      if (targets.isEmpty()) {
        targets.addAll(policy.getDirectories().getPath());
        if (policy.getPds3Directory().getPath() != null) {
          targets.add(policy.getPds3Directory().getPath());
        }
      }
      registryPackage.setDescription("This package contains registration "
          + "of the following targets: " + targets.toString());
    }
    registryPackageGuid = client.publishObject(registryPackage);
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
      System.out.println("\nType 'Harvest -h' for usage");
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
      setupExtractor(policy.getCandidates().getNamespace());
      createRegistryPackage(policy);
      logHeader();
      doHarvesting(policy);
      closeHandlers();
    } catch (JAXBException je) {
      //Don't do anything
    } catch (ParseException pEx) {
      System.err.println("Command-line parse failure: "
            + pEx.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Main class.
   *
   * @param args The command-line arguments.
   */
  public static void main(final String []args) {
    new HarvestLauncher().processMain(args);
  }
}
