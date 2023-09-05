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

package gov.nasa.pds.harvest.search.crawler.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionPhases;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.file.FileObject;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.AccessUrl;
import gov.nasa.pds.registry.model.Slot;

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
   * @param accessUrls A list of access urls.
   */
  public CreateAccessUrlsAction(List<AccessUrl> accessUrls) {
    super();
    this.accessUrls = new ArrayList<AccessUrl>();
    this.accessUrls.addAll(accessUrls);
    String []phases = {CrawlerActionPhases.PRE_INGEST.getName()};
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
      if (metadata.containsKey(Constants.SLOT_METADATA)) {
//        List<Slot> slots = new ArrayList<Slot>();
//        slots.add(new Slot(Constants.ACCESS_URLS, urls));
    	  Metadata meta = new Metadata();
    	  meta.addMetadata(Constants.ACCESS_URLS, urls);
        metadata.addMetadata(Constants.SLOT_METADATA, meta);
      }
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

  /**
   * Create access urls for the given product.
   *
   * @param product The product to create a set of access urls.
   * @param source The source of the given product.
   *
   * @return A list of access urls.
   *
   * @throws IllegalArgumentException If there was an error in creating
   * an access url.
   */
  private List<String> createAccessUrls(File product, File source)
  throws IllegalArgumentException {
    List<String> urls = new ArrayList<String>();
    for (AccessUrl accessUrl : accessUrls) {
      String productFile = product.toString();
      boolean matchedOffset = false;
      for (String offset : accessUrl.getOffset()) {
        if (productFile.startsWith(offset)) {
          productFile = productFile.replaceFirst(Pattern.quote(offset), "")
          .trim();
          matchedOffset = true;
          break;
        }
      }
      if ( (!accessUrl.getOffset().isEmpty()) && (!matchedOffset) ) {
        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
            "Cannot trim path of product '" + product
            + "' as it does not start with any of the supplied offsets: "
            + accessUrl.getOffset(), source));
      }
      productFile = FilenameUtils.separatorsToUnix(productFile);
      String uriString = "";

      if (accessUrl.getBaseUrl().endsWith("/") && productFile.startsWith("/")) {
        uriString = accessUrl.getBaseUrl() + productFile.substring(1);
      } else if (accessUrl.getBaseUrl().endsWith("/") || productFile.startsWith("/")) {
        uriString = accessUrl.getBaseUrl() + productFile;
      } else {
        uriString = accessUrl.getBaseUrl() + "/" + productFile;
      }
      try {
        URI uri = new URI(uriString);
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "Created access url: "
          + uri, source));
        urls.add(uri.toString());
      } catch (URISyntaxException u) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            "Malformed URL syntax '" + uriString + "': " + u.getMessage(),
            source));
      }
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
