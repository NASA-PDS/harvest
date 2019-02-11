// Copyright 2006-2016, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.search.crawler;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionRepo;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds4MetExtractorConfig;
import gov.nasa.pds.harvest.search.inventory.InventoryEntry;
import gov.nasa.pds.harvest.search.inventory.InventoryReader;
import gov.nasa.pds.harvest.search.inventory.InventoryReaderException;
import gov.nasa.pds.harvest.search.inventory.InventoryXMLReader;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;

import java.io.File;
import java.util.logging.Logger;

/**
 * A crawler class for a PDS Bundle file.
 *
 * @author mcayanan
 *
 */
public class BundleCrawler extends CollectionCrawler {
  /** Logger object. */
  private static Logger log = Logger.getLogger(BundleCrawler.class.getName());

  /**
   * Constructor.
   *
   * @param extractorConfig A configuration object for the
   * metadata extractor.
   */
  public BundleCrawler(Pds4MetExtractorConfig extractorConfig) {
    super(extractorConfig);
  }

  /**
   * Crawl a PDS4 bundle file. The bundle will be registered first, then
   * the method will proceed to crawling the collection file it points to.
   *
   * @param bundle The PDS4 bundle file.
   *
   * @throws InventoryReaderException
   */
  public void crawl(File bundle) {
    //Load crawlerActions first before crawling
    CrawlerActionRepo repo = new CrawlerActionRepo();
    repo.loadActions(getActions());
    setActionRepo(repo);
    if (bundle.canRead()) {
      handleFile(bundle);
    } else {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Unreadable target: "
          + bundle));
    }
    try {
      InventoryReader reader = new InventoryXMLReader(bundle);
      for (InventoryEntry entry = new InventoryEntry(); entry != null;) {
        if (!entry.isEmpty()) {
          super.crawl(entry.getFile());
        }
        try {
          entry = reader.getNext();
        } catch (InventoryReaderException ir) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, ir.getMessage(),
              bundle));
        }
      }
    } catch (InventoryReaderException e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, e.getMessage(),
          bundle));
    }
  }
}
