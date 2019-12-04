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

package gov.nasa.pds.harvest.search.crawler.daemon;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.xmlrpc.WebServer;

import gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemon;
import gov.nasa.pds.harvest.search.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;

/**
 * Class that provides the capability to make the Harvest Tool run
 * in persistance mode.
 *
 * @author mcayanan
 *
 */
public class HarvestSolrDaemon extends CrawlDaemon {
  /** To log messages. */
  private static Logger log = Logger.getLogger(
      HarvestSolrDaemon.class.getName());

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
  private int numDocumentsCreated;

  /** Current number of products no registered. */
  private int numDocumentsNotCreated;

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

  /**
   * Constructor
   *
   * @param wait The time in seconds to wait in between crawls.
   * @param crawlers A list of PDSProductCrawler objects to be used during
   * crawler persistance.
   * @param port The port nunmber to be used.
   */
  public HarvestSolrDaemon(int wait, List<PDSProductCrawler> crawlers, int port) {
    super(wait, null, port);
    this.daemonPort = port;
    this.crawlers = new ArrayList<PDSProductCrawler>();
    this.crawlers.addAll(crawlers);

    numBadFiles = 0;
    numFilesSkipped = 0;
    numGoodFiles = 0;
    numDocumentsCreated = 0;
    numDocumentsNotCreated = 0;
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
      printSummary();

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
    int newGoodFiles = HarvestSolrStats.numGoodFiles - numGoodFiles;
    int newBadFiles = HarvestSolrStats.numBadFiles - numBadFiles;
    int newFilesSkipped = HarvestSolrStats.numFilesSkipped - numFilesSkipped;

    int newDocumentsCreated = HarvestSolrStats.numDocumentsCreated
    - numDocumentsCreated;

    int newDocumentsNotCreated = HarvestSolrStats.numDocumentsNotCreated
    - numDocumentsNotCreated;

    int newErrors = HarvestSolrStats.numErrors - numErrors;

    int newWarnings = HarvestSolrStats.numWarnings - numWarnings;

    int newGeneratedChecksumsSameInManifest = HarvestSolrStats.numGeneratedChecksumsSameInManifest - numGeneratedChecksumsSameInManifest;

    int newGeneratedChecksumsDiffInManifest = HarvestSolrStats.numGeneratedChecksumsDiffInManifest - numGeneratedChecksumsDiffInManifest;

    int newGeneratedChecksumsNotCheckedInManifest = HarvestSolrStats.numGeneratedChecksumsNotCheckedInManifest - numGeneratedChecksumsNotCheckedInManifest;

    int newGeneratedChecksumsSameInLabel = HarvestSolrStats.numGeneratedChecksumsSameInLabel - numGeneratedChecksumsSameInLabel;

    int newGeneratedChecksumsDiffInLabel = HarvestSolrStats.numGeneratedChecksumsDiffInLabel - numGeneratedChecksumsDiffInLabel;

    int newGeneratedChecksumsNotCheckedInLabel = HarvestSolrStats.numGeneratedChecksumsNotCheckedInLabel - numGeneratedChecksumsNotCheckedInLabel;

    int newManifestChecksumsSameInLabel = HarvestSolrStats.numManifestChecksumsSameInLabel - numManifestChecksumsSameInLabel;

    int newManifestChecksumsDiffInLabel = HarvestSolrStats.numManifestChecksumsDiffInLabel - numManifestChecksumsDiffInLabel;

    int newManifestChecksumsNotCheckedInLabel = HarvestSolrStats.numManifestChecksumsNotCheckedInLabel - numManifestChecksumsNotCheckedInLabel;

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
          newDocumentsCreated + " of "
          + (newDocumentsCreated + newDocumentsNotCreated)
          + " new documents created."));

      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
          "\nNew Product Types Handled:\n"));
      for (String key : HarvestSolrStats.registeredProductTypes.keySet()) {
        if (registeredProductTypes.containsKey(key)) {
          BigInteger numNewProductTypes =
            HarvestSolrStats.registeredProductTypes.get(key).subtract(registeredProductTypes.get(key));
          if (numNewProductTypes.longValue() != 0) {
            log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              numNewProductTypes + " " + key));
          }
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION,
              HarvestSolrStats.registeredProductTypes.get(key).toString() + " "
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

    }

    // Save the stats for the next round of calculations
    numGoodFiles = HarvestSolrStats.numGoodFiles;
    numBadFiles = HarvestSolrStats.numBadFiles;
    numFilesSkipped = HarvestSolrStats.numFilesSkipped;
    numDocumentsCreated = HarvestSolrStats.numDocumentsCreated;
    numDocumentsNotCreated = HarvestSolrStats.numDocumentsNotCreated;
    numErrors = HarvestSolrStats.numErrors;
    numWarnings = HarvestSolrStats.numWarnings;
    numGeneratedChecksumsSameInManifest = HarvestSolrStats.numGeneratedChecksumsSameInManifest;
    numGeneratedChecksumsDiffInManifest = HarvestSolrStats.numGeneratedChecksumsDiffInManifest;
    numGeneratedChecksumsNotCheckedInManifest = HarvestSolrStats.numGeneratedChecksumsNotCheckedInManifest;
    numGeneratedChecksumsSameInLabel = HarvestSolrStats.numGeneratedChecksumsSameInLabel;
    numGeneratedChecksumsDiffInLabel = HarvestSolrStats.numGeneratedChecksumsDiffInLabel;
    numGeneratedChecksumsNotCheckedInLabel = HarvestSolrStats.numGeneratedChecksumsNotCheckedInLabel;
    numManifestChecksumsSameInLabel = HarvestSolrStats.numManifestChecksumsSameInLabel;
    numManifestChecksumsDiffInLabel = HarvestSolrStats.numManifestChecksumsDiffInLabel;
    numManifestChecksumsNotCheckedInLabel = HarvestSolrStats.numManifestChecksumsNotCheckedInLabel;
    registeredProductTypes.clear();
    registeredProductTypes.putAll(HarvestSolrStats.registeredProductTypes);
  }
}
