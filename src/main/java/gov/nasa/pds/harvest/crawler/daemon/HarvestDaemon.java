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
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.xmlrpc.WebServer;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemon;
import gov.nasa.jpl.oodt.cas.crawl.status.IngestStatus;
import gov.nasa.jpl.oodt.cas.crawl.status.IngestStatus.Result;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.association.AssociationPublisher;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.crawler.actions.FileObjectRegistrationAction;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.security.SecuredUser;

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

  /** The registry url. */
  private String registryUrl;

  /** An authenticated user. */
  private SecuredUser securedUser;

  /** The association publisher. */
  private AssociationPublisher associationPublisher;

  /** Registry ingester. */
  private RegistryIngester ingester;

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
    this.ingester = ingester;
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
      int totalFilesFound = 0;
      // okay, time to crawl
      for (PDSProductCrawler crawler : crawlers) {
        long timeBefore = System.currentTimeMillis();
        crawler.crawl();
        long timeAfter = System.currentTimeMillis();
        setMilisCrawling((long) getMilisCrawling() + (timeAfter - timeBefore));
        setNumCrawls(getNumCrawls() + 1);
        totalFilesFound += crawler.getNumDiscoveredProducts()
        + crawler.getNumBadFiles() + crawler.getNumFilesSkipped();
      }
      for ( Entry<File, Metadata> entry :
        Constants.registeredProducts.entrySet()) {
        associationPublisher.publish(entry.getKey(), entry.getValue());
      }
      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION, totalFilesFound
          + " new file(s) found."));
      //Print out some statistics if new files were found
      if (totalFilesFound != 0) {
        printSummary();
      }
      ingester.clearStats();
      //Make sure to clear the map of registered products
      Constants.registeredProducts.clear();

      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Sleeping for: ["
          + getWaitInterval() + "] seconds"));
      // take a nap
      try {
          Thread.currentThread().sleep(getWaitInterval() * 1000);
      } catch (InterruptedException ignore) {
      }
      for (PDSProductCrawler crawler : crawlers) {
        crawler.clearCrawlStats();
        crawler.clearIngestStatus();
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
    int totalNumDiscoveredProducts = 0;
    int totalNumBadFiles = 0;
    int totalNumFilesSkipped = 0;

    for (PDSProductCrawler crawler : crawlers) {
      totalNumDiscoveredProducts += crawler.getNumDiscoveredProducts();
      totalNumBadFiles += crawler.getNumBadFiles();
      totalNumFilesSkipped += crawler.getNumFilesSkipped();
    }
    int totalFilesProcessed = totalNumDiscoveredProducts + totalNumBadFiles;
    log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
        totalNumDiscoveredProducts
        + " of " + totalFilesProcessed + " file(s) processed, "
        + totalNumFilesSkipped + " skipped"));
    int totalProducts = ingester.getNumProductsRegistered()
    + ingester.getNumProductsNotRegistered();
    log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
        ingester.getNumProductsRegistered() + " of " + totalProducts
        + " products registered."));
    int totalAssociations = ingester.getNumAssociationsRegistered()
    + ingester.getNumAssociationsNotRegistered();
    log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          ingester.getNumAssociationsRegistered() + " of " + totalAssociations
          + " associations registered."));
  }
}
