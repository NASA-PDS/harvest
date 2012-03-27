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
package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;

/**
 * A class to check whether a product's logical identifier (lid) and
 * version ID (vid) have already been registered.
 *
 * @author mcayanan
 *
 */
public class RegistryUniquenessCheckerAction extends CrawlerAction {
  private static Logger log = Logger.getLogger(
          RegistryUniquenessCheckerAction.class.getName());
  private RegistryIngester ingester;
  private String registryUrl;

  private final String ID = "RegistryUniquenessChecker";
  private final String DESCRIPTION =
      "Checks if a product with the same LID and VID exists in the "
      + "registry.";

  /**
   * Constructor.
   *
   * @param registryUrl The URL to the registry service.
   * @param ingester The Registry Ingester.
   */
  public RegistryUniquenessCheckerAction(String registryUrl,
      RegistryIngester ingester) {
    super();
    this.ingester = ingester;
    this.registryUrl = registryUrl;
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
  }

  /**
   * Action that checks to see if a product was already registerd.
   *
   * @param product The product file.
   * @param productMetadata The metadata associatied with the given product.
   *
   * @return 'false' if the product was already registered.
   */
  @Override
  public boolean performAction(File product, Metadata productMetadata)
          throws CrawlerActionException {
    String lid = productMetadata.getMetadata(Constants.LOGICAL_ID);
    String vid = productMetadata.getMetadata(Constants.PRODUCT_VERSION);
    String lidvid = lid + "::" + vid;
    try {
      boolean result;
      result = this.ingester.hasProduct(new URL(this.registryUrl), lid, vid);
      if (result == true) {
        log.log(new ToolsLogRecord(Level.WARNING,
            "Product already exists in the registry: "
            + lidvid, product));
        ++HarvestStats.numProductsNotRegistered;
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, e.getMessage(), product));
      throw new CrawlerActionException(e.getMessage());
    }
  }
}
