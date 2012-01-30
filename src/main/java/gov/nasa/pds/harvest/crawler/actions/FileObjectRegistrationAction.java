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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.registry.exception.RegistryClientException;


/**
 * Class to register file objects.
 *
 * @author mcayanan
 *
 */
public class FileObjectRegistrationAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          AssociationCheckerAction.class.getName());

  /** Crawler action ID. */
  private final String ID = "FileObjectRegistrationAction";

  /** Crawler action description. */
  private final String DESCRIPTION = "Registers file objects associated with "
    + "a product.";

  /** Registry URL. */
  private String registryUrl;

  /** The registry client. */
  private RegistryIngester registryIngester;

  /** A list of actions to perform before file object registration. */
  private List<CrawlerAction> actions;

  /**
   * Constructor.
   *
   * @param registryUrl url of the registry.
   * @param ingester The RegistryIngester.
   * @throws RegistryClientException
   */
  public FileObjectRegistrationAction(String registryUrl,
      RegistryIngester ingester) {
      super();
      String []phases = {CrawlerActionPhases.POST_INGEST_SUCCESS};
      setPhases(Arrays.asList(phases));
      setId(ID);
      setDescription(DESCRIPTION);
      this.registryUrl = registryUrl;
      this.registryIngester = ingester;
      this.actions = new ArrayList<CrawlerAction>();
  }

  /**
   * Perform the action to register the file products.
   *
   * @param product The file.
   * @param metadata The metadata associated with the file.
   *
   * @return Always returns true.
   *
   * @throws CrawlerActionException If an error occurred while processing
   * the file objects.
   */
  @Override
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    for (FileObject fileObject
        : (List<FileObject>) metadata.getAllMetadata(Constants.FILE_OBJECTS)) {
      String lid = metadata.getMetadata(Constants.LOGICAL_ID) + ":"
      + fileObject.getName();
      String vid = metadata.getMetadata(Constants.PRODUCT_VERSION);
      String lidvid = lid + "::" + vid;
      try {
        // Perform a set of actions before ingesting the file object
        for (CrawlerAction action : actions) {
          if (action instanceof StorageIngestAction) {
            StorageIngestAction siAction =
              (StorageIngestAction) action;
            String productId = siAction.performAction(product, fileObject,
                metadata);
            fileObject.setStorageServiceProductId(productId);
          } else if (action instanceof CreateAccessUrlsAction) {
            CreateAccessUrlsAction cauAction = (CreateAccessUrlsAction) action;
            List<String> urls = cauAction.performAction(product, fileObject,
                metadata);
            fileObject.setAccessUrls(urls);
          }
        }
        String guid = registryIngester.ingest(new URL(registryUrl), product,
            fileObject, metadata);
        log.log(new ToolsLogRecord(ToolsLevel.INGEST_SUCCESS,
            "Successfully registered product: " + lidvid, product));
        log.log(new ToolsLogRecord(ToolsLevel.INFO,
            "Product has the following GUID: " + guid, product));
        // Create a reference entry of the file association and add that
          // back to the list of reference entries to be processed later.
        ReferenceEntry refEntry = new ReferenceEntry();
        refEntry.setLogicalID(lid);
        refEntry.setVersion(metadata.getMetadata(Constants.PRODUCT_VERSION));
        refEntry.setGuid(guid);
        refEntry.setType("has_file");
        if (metadata.containsKey(Constants.REFERENCES)) {
          List<ReferenceEntry> refEntries = metadata.getAllMetadata(
              Constants.REFERENCES);
          refEntries.add(refEntry);
          metadata.replaceMetadata(Constants.REFERENCES, refEntries);
        } else {
          List<ReferenceEntry> refEntries = new ArrayList<ReferenceEntry>();
          refEntries.add(refEntry);
          metadata.addMetadata(Constants.REFERENCES, refEntries);
        }
      } catch (IngestException ie) {
        throw new CrawlerActionException(ie.getMessage());
      } catch (MalformedURLException mue) {
        log.log(new ToolsLogRecord(ToolsLevel.INGEST_FAIL, mue.getMessage(),
            product));
        throw new CrawlerActionException(mue.getMessage());
      }
    }
    return true;
  }

  /**
   * Sets a list of crawler actions to perform before file object
   * registration.
   *
   * @param actions A list of crawler actions.
   */
  public void setActions(List<CrawlerAction> actions) {
    this.actions = actions;
  }

}
