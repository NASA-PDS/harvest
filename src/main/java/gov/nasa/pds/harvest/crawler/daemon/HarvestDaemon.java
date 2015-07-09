// Copyright 2006-2015, by the California Institute of Technology.
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.xmlrpc.WebServer;

import gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemon;
import gov.nasa.pds.harvest.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
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

  /** Current number of generated checksums matching their supplied value. */
  private int numGeneratedChecksumsSameInManifest;

  /** Current number of generated checksums not matching their supplied value.
   */
  private int numGeneratedChecksumsDiffInManifest;

  /** Current number of generated checksums not checked. */
  private int numGeneratedChecksumsNotCheckedInManifest;

  /** Current number of generated checksums matching their supplied value. */
  private int numGeneratedChecksumsSameInLabel;

  /** Current number of generated checksums not matching their supplied value.
   */
  private int numGeneratedChecksumsDiffInLabel;

  /** Current number of generated checksums not checked. */
  private int numGeneratedChecksumsNotCheckedInLabel;

  /** Current number of generated checksums matching their supplied value. */
  private int numManifestChecksumsSameInLabel;

  /** Current number of generated checksums not matching their supplied value.
   */
  private int numManifestChecksumsDiffInLabel;

  /** Current number of generated checksums not checked. */
  private int numManifestChecksumsNotCheckedInLabel;

  /** Mapping of the current count of registered product types. */
  private HashMap<String, BigInteger> registeredProductTypes;

  /** Registry ingester. */
  private RegistryIngester ingester;

  /**
   * Constructor
   *
   * @param wait The time in seconds to wait in between crawls.
   * @param crawlers A list of PDSProductCrawler objects to be used during
   * crawler persistance.
   * @param port The port nunmber to be used.
   */
  public HarvestDaemon(int wait, List<PDSProductCrawler> crawlers, int port,
      RegistryIngester ingester) {
    super(wait, null, port);
    this.daemonPort = port;
    this.crawlers = new ArrayList<PDSProductCrawler>();
    this.crawlers.addAll(crawlers);
    this.ingester = ingester;

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
    numGeneratedChecksumsSameInManifest = 0;
    numGeneratedChecksumsDiffInManifest = 0;
    numGeneratedChecksumsNotCheckedInManifest = 0;
    numGeneratedChecksumsSameInLabel = 0;
    numGeneratedChecksumsDiffInLabel = 0;
    numGeneratedChecksumsNotCheckedInLabel = 0;
    numManifestChecksumsSameInLabel = 0;
    numManifestChecksumsDiffInLabel = 0;
    numManifestChecksumsNotCheckedInLabel = 0;
    registeredProductTypes = new HashMap<String, BigInteger>();
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
      // May need to ingest any leftover products sitting in the queue
      ingester.getBatchManager().ingest();
/*
      for ( Entry<File, Metadata> entry :
        Constants.registeredProducts.entrySet()) {
        associationPublisher.publish(entry.getKey(), entry.getValue());
      }
      // Need to ingest any leftover products sitting in the queue
      ingester.getBatchManager().ingest();
*/
      printSummary();

      //Make sure to clear the map of registered products
//      Constants.registeredProducts.clear();

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

    int newGeneratedChecksumsSameInManifest = HarvestStats.numGeneratedChecksumsSameInManifest - numGeneratedChecksumsSameInManifest;

    int newGeneratedChecksumsDiffInManifest = HarvestStats.numGeneratedChecksumsDiffInManifest - numGeneratedChecksumsDiffInManifest;

    int newGeneratedChecksumsNotCheckedInManifest = HarvestStats.numGeneratedChecksumsNotCheckedInManifest - numGeneratedChecksumsNotCheckedInManifest;

    int newGeneratedChecksumsSameInLabel = HarvestStats.numGeneratedChecksumsSameInLabel - numGeneratedChecksumsSameInLabel;

    int newGeneratedChecksumsDiffInLabel = HarvestStats.numGeneratedChecksumsDiffInLabel - numGeneratedChecksumsDiffInLabel;

    int newGeneratedChecksumsNotCheckedInLabel = HarvestStats.numGeneratedChecksumsNotCheckedInLabel - numGeneratedChecksumsNotCheckedInLabel;

    int newManifestChecksumsSameInLabel = HarvestStats.numManifestChecksumsSameInLabel - numManifestChecksumsSameInLabel;

    int newManifestChecksumsDiffInLabel = HarvestStats.numManifestChecksumsDiffInLabel - numManifestChecksumsDiffInLabel;

    int newManifestChecksumsNotCheckedInLabel = HarvestStats.numManifestChecksumsNotCheckedInLabel - numManifestChecksumsNotCheckedInLabel;

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
          BigInteger numNewProductTypes =
            HarvestStats.registeredProductTypes.get(key).subtract(registeredProductTypes.get(key));
          if (numNewProductTypes.longValue() != 0) {
            log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              numNewProductTypes + " " + key));
          }
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              HarvestStats.registeredProductTypes.get(key).toString() + " "
              + key));
        }
      }

      int totalGeneratedChecksumsVsManifest =
        newGeneratedChecksumsSameInManifest
        + newGeneratedChecksumsDiffInManifest;

      if ( (totalGeneratedChecksumsVsManifest != 0)
          || (newGeneratedChecksumsNotCheckedInManifest != 0) ) {
        log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
            "\n" + newGeneratedChecksumsSameInManifest
            + " of " + totalGeneratedChecksumsVsManifest
            + " generated checksums matched "
            + "their supplied value in the manifest, "
            + newGeneratedChecksumsNotCheckedInManifest
            + " generated value(s) not checked\n"));
      }

      int totalGeneratedChecksumsVsLabel =
        newGeneratedChecksumsSameInLabel
        + newGeneratedChecksumsDiffInLabel;

      if ( (totalGeneratedChecksumsVsLabel != 0)
          || (newGeneratedChecksumsNotCheckedInLabel != 0) ) {
        log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
            "\n" + newGeneratedChecksumsSameInLabel
            + " of " + totalGeneratedChecksumsVsLabel
            + " generated checksums matched "
            + "the supplied value in their product label, "
            + newGeneratedChecksumsNotCheckedInLabel
            + " generated value(s) not checked\n"));
      }

      int totalManifestChecksumsVsLabel =
        newManifestChecksumsSameInLabel
        + newManifestChecksumsDiffInLabel;

      if ( (totalManifestChecksumsVsLabel != 0)
          || (newManifestChecksumsNotCheckedInLabel != 0) ) {
        log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
            "\n" + newManifestChecksumsSameInLabel
            + " of " + totalManifestChecksumsVsLabel
            + " checksums in the manifest matched "
            + "the supplied value in their product label, "
            + newManifestChecksumsNotCheckedInLabel
            + " value(s) not checked\n"));
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
    numGeneratedChecksumsSameInManifest = HarvestStats.numGeneratedChecksumsSameInManifest;
    numGeneratedChecksumsDiffInManifest = HarvestStats.numGeneratedChecksumsDiffInManifest;
    numGeneratedChecksumsNotCheckedInManifest = HarvestStats.numGeneratedChecksumsNotCheckedInManifest;
    numGeneratedChecksumsSameInLabel = HarvestStats.numGeneratedChecksumsSameInLabel;
    numGeneratedChecksumsDiffInLabel = HarvestStats.numGeneratedChecksumsDiffInLabel;
    numGeneratedChecksumsNotCheckedInLabel = HarvestStats.numGeneratedChecksumsNotCheckedInLabel;
    numManifestChecksumsSameInLabel = HarvestStats.numManifestChecksumsSameInLabel;
    numManifestChecksumsDiffInLabel = HarvestStats.numManifestChecksumsDiffInLabel;
    numManifestChecksumsNotCheckedInLabel = HarvestStats.numManifestChecksumsNotCheckedInLabel;
    registeredProductTypes.clear();
    registeredProductTypes.putAll(HarvestStats.registeredProductTypes);
  }
}
