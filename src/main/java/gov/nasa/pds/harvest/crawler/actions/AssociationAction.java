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
package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;
import gov.nasa.pds.harvest.util.Utility;
import gov.nasa.pds.registry.exception.RegistryClientException;
import gov.nasa.pds.registry.model.Association;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.Slot;

/**
 * Class to publish associations to the PDS Registry Service upon
 * successful ingestion of the product.
 *
 * @author mcayanan
 *
 */
public class AssociationAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      AssociationAction.class.getName());

  /** Crawler action ID. */
  private final String ID = "AssociationRegistrationAction";

  /** Crawler action description. */
  private final String DESCRIPTION = "Registers associations associated with "
    + "a registered product.";

  /** The registry url. */
  private URL registryUrl;

  /** The registry ingester. */
  private RegistryIngester registryIngester;

  /**
   * Constructor.
   *
   * @param registryUrl The URL to the registry service.
   * @param ingester The registry ingester.
   *
   * @throws RegistryClientException
   * @throws MalformedURLException
   */
  public AssociationAction(String registryUrl, RegistryIngester ingester)
  throws MalformedURLException {
    super();
    String []phases = {CrawlerActionPhases.POST_INGEST_SUCCESS};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
    this.registryUrl = new URL(registryUrl);
    this.registryIngester = ingester;
  }

  /**
   * Publish the association.
   *
   * @param product The file containing the associations.
   * @param productMetadata The metadata associated with the given product.
   *
   * @return 'true' if the associations were registered successfully.
   *
   */
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    boolean passFlag = false;

    if (!metadata.containsKey(Constants.REFERENCES)) {
      return passFlag;
    }

    for (ReferenceEntry refEntry: (List<ReferenceEntry>)
        metadata.getAllMetadata(Constants.REFERENCES)) {
      try {
        Association association = createAssociation(product, metadata,
            refEntry);
        String targetReference = refEntry.getLogicalID();
        if (refEntry.hasVersion()) {
          targetReference += "::" + refEntry.getVersion();
        }
        String guid = registryIngester.ingest(registryUrl, product,
            association, targetReference);
        passFlag = true;
      } catch (IngestException i) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            i.getMessage(), product));
        ++HarvestStats.numAssociationsNotRegistered;
      }
    }
    return passFlag;
  }

    /**
     * Creates an association.
     *
     * @param product The file containing the associations.
     * @param metadata The metadata associated with the given product.
     * @param refEntry A reference entry in the product label.
     *
     * @return An association for the given product.
     * @throws IngestException
     */
  private Association createAssociation(File product, Metadata metadata,
      ReferenceEntry refEntry) throws IngestException {
    Association association = new Association();
    Boolean verifiedFlag = false;

    association.setSourceObject(metadata.getMetadata(Constants.PRODUCT_GUID));
    association.setAssociationType(refEntry.getType());
    if (refEntry.hasGuid()) {
      association.setTargetObject(refEntry.getGuid());
      verifiedFlag = true;
    } else {
      //Check to see if the target product is in the registry.
      //If it isn't, then the target GUID will be the LIDVID reference
      //of the target.
      ExtrinsicObject target = registryIngester.getExtrinsic(registryUrl,
          refEntry.getLogicalID(), refEntry.getVersion());
      String lidvid = refEntry.getLogicalID() + "::" + refEntry.getVersion();
      if (target != null) {
        association.setTargetObject(target.getGuid());
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "Found registered "
          + "product for reference: " + lidvid, product));
        verifiedFlag = true;
      } else {
        association.setTargetObject(lidvid);
        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
          "Product not found in registry for reference: "
          + lidvid + ". LIDVID will be used as the target reference "
          + "for the association.", product));
      }
    }
    Set<Slot> slots = new HashSet<Slot>();
    slots.add(new Slot("verified", Arrays.asList(
      new String[]{verifiedFlag.toString()}))
    );
    association.setSlots(slots);
    if (log.getParent().getHandlers()[0].getLevel().intValue()
        <= ToolsLevel.DEBUG.intValue()) {
      try {
        log.log(new ToolsLogRecord(ToolsLevel.DEBUG,
            "Association contents: \n" + Utility.toXML(association)));
      } catch (JAXBException je) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, je.getMessage()));
      }
    }
    return association;
  }
}
