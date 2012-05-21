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
import gov.nasa.pds.harvest.policy.AccessUrl;

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
  private List<AccessUrl> accessUrls;

  /** Crawler action identifier. */
  private final static String ID = "CreateAccessUrlsAction";

  /** Crawler action description. */
  private final static String DESCRIPTION = "Creates access urls to access "
    + "the registered products.";

  private boolean registerFileUrls;

  /**
   * Constructor.
   *
   * @param baseUrls A list of base urls.
   */
  public CreateAccessUrlsAction(List<AccessUrl> accessUrls) {
    super();
    this.accessUrls = new ArrayList<AccessUrl>();
    this.accessUrls.addAll(accessUrls);
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
    registerFileUrls = false;
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
    List<String> urls = new ArrayList<String>();
    try {
      urls.addAll(createAccessUrls(product, product));
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
          ExceptionUtils.getRootCauseMessage(e), product));
      throw new CrawlerActionException(
          ExceptionUtils.getRootCauseMessage(e));
    }
    if (!urls.isEmpty()) {
      metadata.addMetadata(Constants.ACCESS_URLS, urls);
    }
    return true;
  }

  /**
   * Create access urls for the given file object.
   *
   * @param product The file associated with the given file object.
   * @param fileObject The file object.
   *
   * @return a list of access urls.
   */
  public List<String> performAction(File product, FileObject fileObject) {
    List<String> urls = new ArrayList<String>();
    File fileSpec = new File(fileObject.getLocation(), fileObject.getName());
    try {
      urls.addAll(createAccessUrls(fileSpec, product));
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
          ExceptionUtils.getRootCauseMessage(e), product));
    }
    return urls;
  }

  private List<String> createAccessUrls(File product, File source)
  throws IllegalArgumentException {
    List<String> urls = new ArrayList<String>();
    for (AccessUrl accessUrl : accessUrls) {
      String productFile = product.toString();
      if (accessUrl.getOffset() != null) {
        if (productFile.startsWith(accessUrl.getOffset())) {
          productFile = productFile.replaceFirst(accessUrl.getOffset(), "")
          .trim();
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Cannot trim path of file specification '" + product
              + "' as it does not start with the " + "supplied offset: "
              + accessUrl.getOffset(), source));
        }
      }
      URI uri = UriBuilder.fromUri(accessUrl.getBaseUrl()).path(
          FilenameUtils.separatorsToUnix(productFile)).build();
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Created access url: "
          + uri, source));
      urls.add(uri.toString());
    }
    if (registerFileUrls) {
      URI uri = UriBuilder.fromPath("file://"
          + FilenameUtils.separatorsToUnix(product.toString())).build();
      urls.add(uri.toString());
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Created access url: "
          + uri, source));
    }
    return urls;
  }

  public void setRegisterFileUrls(boolean registerFileUrls) {
    this.registerFileUrls = registerFileUrls;
  }
}
