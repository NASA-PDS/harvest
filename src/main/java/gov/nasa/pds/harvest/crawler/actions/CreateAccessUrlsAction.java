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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;

/**
 * Class that creates access urls based on a given set of base urls.
 *
 * @author mcayanan
 *
 */
public class CreateAccessUrlsAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          CreateAccessUrlsAction.class.getName());

  /** A list of base urls from which to start forming an access url. */
  private List<String> baseUrls;

  /** Crawler action identifier. */
  private final static String ID = "CreateAccessUrlsAction";

  /** Crawler action description. */
  private final static String DESCRIPTION = "Creates access urls to access "
    + "the registered products.";

  /**
   * Constructor.
   *
   * @param baseUrls A list of base urls.
   */
  public CreateAccessUrlsAction(List<String> baseUrls) {
    super();
    this.baseUrls = new ArrayList<String>();
    this.baseUrls.addAll(baseUrls);
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
  }

  /**
   * Perform the action to create a set of access urls for the given product.
   *
   * @param product A PDS product.
   * @param metadata The metadata associated with the product.
   *
   * @return true if the action was successful.
   */
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    List<String> accessUrls = new ArrayList<String>();
    for (String baseUrl : baseUrls) {
      try {
        //The separatorsToUnix method ensures that the path separators
        //will always be '/' in order to correctly form the uri.
        URI uri = UriBuilder.fromUri(baseUrl).path(
            FilenameUtils.separatorsToUnix(product.toString()))
            .build();
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "Created access url: "
            + uri, product));
        accessUrls.add(uri.toString());
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            ExceptionUtils.getRootCauseMessage(e), product));
        throw new CrawlerActionException(
            ExceptionUtils.getRootCauseMessage(e));
      }
    }
    if (!accessUrls.isEmpty()) {
      metadata.addMetadata(Constants.ACCESS_URLS, accessUrls);
    }
    return true;
  }

  /**
   * Create access urls for the given file object.
   *
   * @param product The file associated with the given file object.
   * @param fileObject The file object.
   * @param metadata The metadata associated with the product.
   *
   * @return a list of access urls.
   */
  public List<String> performAction(File product, FileObject fileObject,
      Metadata metadata) {
    List<String> accessUrls = new ArrayList<String>();
    for (String baseUrl : baseUrls) {
      try {
        URI uri = UriBuilder.fromUri(baseUrl).path(
          FilenameUtils.separatorsToUnix(new File(fileObject.getLocation(),
              fileObject.getName()).toString())).build();
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "Created access url: "
            + uri, product));
        accessUrls.add(uri.toString());
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            ExceptionUtils.getRootCauseMessage(e), product));
      }
    }
    return accessUrls;
  }
}
