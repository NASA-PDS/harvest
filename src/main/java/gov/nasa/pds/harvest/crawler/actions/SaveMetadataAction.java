// Copyright 2006-2011, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.util.Arrays;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;

/**
 * Class that caches the registered product and metadata needed to register
 * associations at a later time.
 *
 * @author mcayanan
 *
 */
public class SaveMetadataAction extends CrawlerAction {
  /** The ID of the crawler action. */
  private final String ID = "SaveMetadataAction";

  /** A description of the crawler action. */
  private final String DESCRIPTION = "Saves the guids of registered products"
    + " and the reference entries to be processed after the crawling process.";

  /**
   * Constructor.
   *
   */
  public SaveMetadataAction() {
    String []phases = {CrawlerActionPhases.POST_INGEST_SUCCESS};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
  }

  /**
   * Perform the action to cache the registered product along with metadata
   * needed to register the associations.
   *
   * @param product The registered product.
   * @param metadata The product metadata.
   *
   * @throws CrawlerActionException If an error occurred while saving the
   * metadata.
   */
  @Override
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    Metadata m = new Metadata();
    m.addMetadata(Constants.PRODUCT_GUID,
        metadata.getMetadata(Constants.PRODUCT_GUID));
    m.addMetadata(Constants.REFERENCES, metadata.getAllMetadata(
        Constants.REFERENCES));

    Constants.registeredProducts.put(product, m);

    return true;
  }

}
