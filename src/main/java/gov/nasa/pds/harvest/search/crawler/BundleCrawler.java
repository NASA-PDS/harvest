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

package gov.nasa.pds.harvest.search.crawler;

import org.apache.oodt.cas.crawl.action.CrawlerActionRepo;
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
    repo.loadActionsFromBeanFactory(getApplicationContext(), getActions());
    repo.getActions();
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
