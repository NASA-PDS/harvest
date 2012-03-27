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
package gov.nasa.pds.harvest.crawler.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.xmlrpc.WebServer;

import gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemon;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.association.AssociationPublisher;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.security.SecuredUser;
import gov.nasa.pds.harvest.stats.HarvestStats;

/**
 * Class that provides the capability to make the Harvest Tool run
 * in persistance mode.
 *
 * @author mcayanan
 *
 */
public class HarvestDaemon extends CrawlDaemon {
  /** To log messages. */
  private static Logger log = Logger.getLogger(
      HarvestDaemon.class.getName());

  /** The port number to be used. */
  private int daemonPort;

  /** A list of crawlers. */
  private List<PDSProductCrawler> crawlers;

  /** The association publisher. */
  private AssociationPublisher associationPublisher;

  /** Current number of good files. */
  private int numGoodFiles;

  /** Current number of bad files. */
  private int numBadFiles;

  /** Current number of files skipped. */
  private int numFilesSkipped;

  /** Current number of products registered. */
  private int numProductsRegistered;

  /** Current number of products no registered. */
  private int numProductsNotRegistered;

  /** Current number of ancillary products registered. */
  private int numAncillaryProductsRegistered;

  /** Current number of ancillary products not registered. */
  private int numAncillaryProductsNotRegistered;

  /** Current number of associations registered. */
  private int numAssociationsRegistered;

  /** Current number of associations not registered. */
  private int numAssociationsNotRegistered;

  /** Current number of errors. */
  private int numErrors;

  /** Current number of warnings. */
  private int numWarnings;

  private HashMap<String, List<File>> registeredProductTypes;

  /**
   * Constructor
   *
   * @param wait The time in seconds to wait in between crawls.
   * @param crawlers A list of PDSProductCrawler objects to be used during
   * crawler persistance.
   * @param port The port nunmber to be used.
   * @param associationPublisher An AssociationPublisher object to process
   * the associations.
   */
  public HarvestDaemon(int wait, List<PDSProductCrawler> crawlers, int port,
      AssociationPublisher associationPublisher, RegistryIngester ingester) {
    super(wait, null, port);
    this.daemonPort = port;
    this.associationPublisher = associationPublisher;
    this.crawlers = new ArrayList<PDSProductCrawler>();
    this.crawlers.addAll(crawlers);

    numAncillaryProductsNotRegistered = 0;
    numAncillaryProductsRegistered = 0;
    numAssociationsNotRegistered = 0;
    numAssociationsRegistered = 0;
    numBadFiles = 0;
    numFilesSkipped = 0;
    numGoodFiles = 0;
    numProductsRegistered = 0;
    numProductsNotRegistered = 0;
    numErrors = 0;
    numWarnings = 0;
    registeredProductTypes = new HashMap<String, List<File>>();
  }

  /**
   * Starts the crawling mechanism.
   *
   */
  public void startCrawling() {
    WebServer server = new WebServer(daemonPort);
    server.addHandler("crawldaemon", this);
    server.start();

    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Starting crawler daemon."));
    for (PDSProductCrawler crawler : crawlers) {
      crawler.setInPersistanceMode(true);
    }

    while (isRunning()) {
      // okay, time to crawl
      for (PDSProductCrawler crawler : crawlers) {
        long timeBefore = System.currentTimeMillis();
        crawler.crawl();
        long timeAfter = System.currentTimeMillis();
        setMilisCrawling((long) getMilisCrawling() + (timeAfter - timeBefore));
        setNumCrawls(getNumCrawls() + 1);
      }
      for ( Entry<File, Metadata> entry :
        Constants.registeredProducts.entrySet()) {
        associationPublisher.publish(entry.getKey(), entry.getValue());
      }
      printSummary();

      //Make sure to clear the map of registered products
      Constants.registeredProducts.clear();

      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Sleeping for: ["
          + getWaitInterval() + "] seconds"));

      // take a nap
      try {
        Thread.currentThread().sleep(getWaitInterval() * 1000);
      } catch (InterruptedException ignore) {
      }
    }
    for (PDSProductCrawler crawler : crawlers) {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Crawl Daemon: Shutting "
        + "down gracefully", crawler.getProductPath()));
    }
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Total num Crawls: ["
        + getNumCrawls() + "]"));
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Total time spent crawling: "
        + "[" + (getMilisCrawling() / 1000.0) + "] seconds"));
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Average Crawl Time: ["
        + (getAverageCrawlTime() / 1000.0) + "] seconds"));
    server.shutdown();
  }

  /**
   * Prints a summary of the crawl.
   *
   */
  private void printSummary() {
    // Calculate the stats during this particular harvest instance
    int newGoodFiles = HarvestStats.numGoodFiles - numGoodFiles;
    int newBadFiles = HarvestStats.numBadFiles - numBadFiles;
    int newFilesSkipped = HarvestStats.numFilesSkipped - numFilesSkipped;

    int newProductsRegistered = HarvestStats.numProductsRegistered
    - numProductsRegistered;

    int newProductsNotRegistered = HarvestStats.numProductsNotRegistered
    - numProductsNotRegistered;

    int newAncillaryProductsRegistered =
      HarvestStats.numAncillaryProductsRegistered
      - numAncillaryProductsRegistered;

    int newAncillaryProductsNotRegistered =
      HarvestStats.numAncillaryProductsNotRegistered
      - numAncillaryProductsNotRegistered;

    int newAssociationsRegistered = HarvestStats.numAssociationsRegistered
    - numAssociationsRegistered;

    int newAssociationsNotRegistered =
      HarvestStats.numAssociationsNotRegistered - numAssociationsNotRegistered;

    int newErrors = HarvestStats.numErrors - numErrors;

    int newWarnings = HarvestStats.numWarnings - numWarnings;

    log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
        + (newGoodFiles + newBadFiles + newFilesSkipped)
        + " new file(s) found."));

    if ( (newGoodFiles + newBadFiles + newFilesSkipped) == 0) {
      return;
    } else {
      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          newGoodFiles + " of "
          + (newGoodFiles + newBadFiles) + " new file(s) processed, "
          + newFilesSkipped + " other file(s) skipped"));

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          newErrors + " new error(s), " + newWarnings + " new warning(s)"
          + "\n"));

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          newProductsRegistered + " of "
          + (newProductsRegistered + newProductsNotRegistered)
          + " new products registered."));

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          newAncillaryProductsRegistered + " of "
          + (newAncillaryProductsRegistered + newAncillaryProductsNotRegistered)
          + " new ancillary products registered."));

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          "\nNew Product Types Registered:\n"));
      for (String key : HarvestStats.registeredProductTypes.keySet()) {
        if (registeredProductTypes.containsKey(key)) {
          int numNewProductTypes =
            HarvestStats.registeredProductTypes.get(key).size()
            - registeredProductTypes.get(key).size();
          if (numNewProductTypes != 0) {
            log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              numNewProductTypes + " " + key));
          }
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              HarvestStats.registeredProductTypes.get(key).size() + " "
              + key));
        }
      }

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION, "\n"
          + newAssociationsRegistered + " of "
          + (newAssociationsRegistered + newAssociationsNotRegistered)
          + " new associations registered.\n"));
    }

    // Save the stats for the next round of calculations
    numGoodFiles = HarvestStats.numGoodFiles;
    numBadFiles = HarvestStats.numBadFiles;
    numFilesSkipped = HarvestStats.numFilesSkipped;
    numProductsRegistered = HarvestStats.numProductsRegistered;
    numProductsNotRegistered = HarvestStats.numProductsNotRegistered;
    numAncillaryProductsRegistered =
      HarvestStats.numAncillaryProductsRegistered;
    numAncillaryProductsNotRegistered =
      HarvestStats.numAncillaryProductsNotRegistered;
    numAssociationsRegistered = HarvestStats.numAssociationsRegistered;
    numAssociationsNotRegistered = HarvestStats.numAssociationsNotRegistered;
    numErrors = HarvestStats.numErrors;
    numWarnings = HarvestStats.numWarnings;
    registeredProductTypes.clear();
    registeredProductTypes.putAll(HarvestStats.registeredProductTypes);
  }
}
