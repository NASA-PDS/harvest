// Copyright 2006-2019, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.search;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.pds.harvest.search.policy.Manifest;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.crawler.CollectionCrawler;
import gov.nasa.pds.harvest.search.crawler.PDS3FileCrawler;
import gov.nasa.pds.harvest.search.crawler.PDS3ProductCrawler;
import gov.nasa.pds.harvest.search.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.search.crawler.actions.CreateAccessUrlsAction;
import gov.nasa.pds.harvest.search.crawler.actions.CreateSearchDocAction;
import gov.nasa.pds.harvest.search.crawler.actions.FileObjectRegistrationAction;
import gov.nasa.pds.harvest.search.crawler.actions.StorageIngestAction;
import gov.nasa.pds.harvest.search.crawler.daemon.HarvestSolrDaemon;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds3MetExtractorConfig;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds4MetExtractorConfig;
import gov.nasa.pds.harvest.search.doc.SearchDocState;
import gov.nasa.pds.harvest.search.file.ChecksumManifest;
import gov.nasa.pds.harvest.search.ingest.SearchIngester;
import gov.nasa.pds.harvest.search.policy.Policy;
import gov.nasa.pds.registry.exception.RegistryClientException;
import gov.nasa.pds.search.core.exception.SearchCoreException;
import gov.nasa.pds.search.core.exception.SearchCoreFatalException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;

/**
 * Front end class to the Harvest tool.
 *
 * @author mcayanan
 *
 */
public class HarvesterSearch {

  /** The port number to use for the daemon if running Harvest in continuous
   *  mode.
   */
  private int daemonPort;

  /** The wait interval in seconds in between crawls when running Harvest in
   *  continuous mode.
   */
  private int waitInterval;

  /** CrawlerAction that performs file object registration. */
  private FileObjectRegistrationAction fileObjectRegistrationAction;
  
  private File configDir;
  
  private File outputDir;
  
  /** An ingester for the PDS Registry/Search Service. */
  private SearchIngester ingester;

  private String searchUrl;
  
  private SearchDocState searchDocState;
  
  private File registeredResources;
  
  /**
   * Constructor.
   *
   * @param searchUrl The search service location.
   * @param configDir Top level directory to the Search Core configuration files.
   * @param outputDir Directory location of the generated Solr documents.
   * @param resources JSON file used as a lookup table for populating resources.
   *
   *
   */
  public HarvesterSearch(String searchUrl, File configDir, File outputDir, 
      File resources) {
    this.daemonPort = -1;
    this.waitInterval = -1;
    this.fileObjectRegistrationAction = new FileObjectRegistrationAction();
    this.configDir = configDir;
    this.outputDir = outputDir;
    this.ingester = new SearchIngester(searchUrl);
    this.searchUrl = searchUrl;
    this.searchDocState = new SearchDocState();
    this.registeredResources = resources;
  }

  /**
   * Sets the daemon port.
   *
   * @param port The port number to use.
   */
  public void setDaemonPort(int port) {
    this.daemonPort = port;
  }

  /**
   * Sets the wait interval in seconds in between crawls.
   *
   * @param interval The wait interval in seconds.
   */
  public void setWaitInterval(int interval) {
    this.waitInterval = interval;
  }

  /**
   * Get the default crawler actions.
   *
   * @return A list of default crawler actions.
   * @throws ConnectionException
   * @throws MalformedURLException
   * @throws SearchCoreFatalException 
   * @throws SearchCoreException 
   * @throws RegistryClientException
   */
  private List<CrawlerAction> getDefaultCrawlerActions(Policy policy,
      PDSProductCrawler crawler)
  throws MalformedURLException, ConnectionException, SearchCoreException, 
  SearchCoreFatalException {
    List<CrawlerAction> ca = new ArrayList<CrawlerAction>();
    List<CrawlerAction> fileObjectRegistrationActions =
      new ArrayList<CrawlerAction>();
    ca.add(fileObjectRegistrationAction);
    if (policy.getStorageIngestion() != null) {
      CrawlerAction fmAction = new StorageIngestAction(
          new URL(policy.getStorageIngestion().getServerUrl()));
      ca.add(fmAction);
      fileObjectRegistrationActions.add(fmAction);
    }
    if ( (policy.getAccessUrls().isRegisterFileUrls() != false) ||
        (!policy.getAccessUrls().getAccessUrl().isEmpty()) ) {
      CreateAccessUrlsAction cauAction = new CreateAccessUrlsAction(
          policy.getAccessUrls().getAccessUrl());
      cauAction.setRegisterFileUrls(
          policy.getAccessUrls().isRegisterFileUrls());
      ca.add(cauAction);
      fileObjectRegistrationActions.add(cauAction);
    }
    fileObjectRegistrationAction.setActions(fileObjectRegistrationActions);
    //Set the flag to generate checksums
    fileObjectRegistrationAction.setGenerateChecksums(
        policy.getChecksums().isGenerate());
    fileObjectRegistrationAction.setFileTypes(policy.getFileTypes());
    //This is the last action that should be performed.
//    ca.add(new SaveMetadataAction());
    // Remove the File Object Registration crawler action if this is
    // a Product_File_Repository Crawler
    if (crawler instanceof PDS3FileCrawler) {
      ca.remove(fileObjectRegistrationAction);
    }
    CreateSearchDocAction createSearchDoc = new CreateSearchDocAction(
        configDir, outputDir, this.registeredResources, this.searchDocState);
    if (crawler instanceof CollectionCrawler) {
      createSearchDoc.setCacheCollection(true);
    }
    ca.add(createSearchDoc);
    return ca;
  }

  /**
   * Harvest the products specified in the given policy.
   *
   * @param policy An object representation of the configuration file that
   *  specifies what to harvest.
   *
   * @throws ConnectionException
   * @throws IOException
   * @throws SearchCoreFatalException 
   * @throws SearchCoreException 
   */
  public void harvest(Policy policy) throws IOException, ConnectionException,
  SearchCoreException, SearchCoreFatalException {
    boolean doCrawlerPersistance = false;
    Map<File, String> checksums = new HashMap<File, String>();
    if (policy.getChecksums().getManifest() != null) {
      Manifest manifest = policy.getChecksums().getManifest();
      ChecksumManifest cm = new ChecksumManifest(
          manifest.getBasePath());      
      checksums.putAll(cm.read(new File(manifest.getValue()))); 
      fileObjectRegistrationAction.setChecksumManifest(checksums);
    }
    if (waitInterval != -1 && daemonPort != -1) {
      doCrawlerPersistance = true;
    }
    Pds4MetExtractorConfig pds4MetExtractorConfig = new Pds4MetExtractorConfig(
        policy.getCandidates().getProductMetadata(),
        policy.getReferences());
    List<PDSProductCrawler> crawlers = new ArrayList<PDSProductCrawler>();
    // Crawl collections
    int count = -1;
    for (String collection : policy.getCollections().getFile()) {
      CollectionCrawler cc = new CollectionCrawler(pds4MetExtractorConfig);
      cc.setProductPath(collection);

      crawlers.add(cc);
    }
    // Crawl directories
    for (String directory : policy.getDirectories().getPath()) {
      PDSProductCrawler pc = new PDSProductCrawler(pds4MetExtractorConfig);
      pc.setProductPath(directory);
      if (policy.getDirectories().getFileFilter() != null) {
        pc.setFileFilter(policy.getDirectories().getFileFilter());
      }
      if (policy.getDirectories().getDirectoryFilter() != null) {
        pc.setDirectoryFilter(policy.getDirectories().getDirectoryFilter());
      }
      crawlers.add(pc);
    }
    // Crawl a PDS3 directory
    for (String directory : policy.getPds3Directories().getPath()) {
      PDS3ProductCrawler p3c = new PDS3ProductCrawler();
      // If the objectType attribute was set to "Product_File_Repository",
      // then the tool should just register everything as a
      // Product_File_Repository product.
      String pds3ObjectType = policy.getCandidates().getPds3ProductMetadata()
          .getObjectType();
      if (pds3ObjectType != null
          && pds3ObjectType.equals(Constants.FILE_OBJECT_PRODUCT_TYPE)) {
        PDS3FileCrawler crawler = new PDS3FileCrawler();
        crawler.setChecksumManifest(checksums);
        crawler.setGenerateChecksums(policy.getChecksums().isGenerate());
        p3c = crawler;
      }
      Pds3MetExtractorConfig pds3MetExtractorConfig =
        new Pds3MetExtractorConfig(policy.getCandidates()
            .getPds3ProductMetadata());
      p3c.setPDS3MetExtractorConfig(pds3MetExtractorConfig);
      p3c.setProductPath(directory);
      if (policy.getPds3Directories().getFileFilter() != null) {
        p3c.setFileFilter(policy.getPds3Directories().getFileFilter());
      }
      if (policy.getPds3Directories().getDirectoryFilter() != null) {
        p3c.setDirectoryFilter(
            policy.getPds3Directories().getDirectoryFilter());
      }
      crawlers.add(p3c);
    }
    // Perform crawl while looping through the crawler list if
    // crawler persistance is disabled.
    for (PDSProductCrawler crawler : crawlers) {
      crawler.addActions(getDefaultCrawlerActions(policy, crawler));
      crawler.setSearchUrl(searchUrl);
      crawler.setIngester(ingester);
      crawler.setCounter(this.searchDocState);
     // crawler.setSearchDocGenerator(new SearchDocGenerator(configDir, outputDir));
      if (!doCrawlerPersistance) {
        crawler.crawl();
      }
    }
    // If crawler persistance is enabled, use the HarvestDaemon object to
    // do the crawling
    
    if (doCrawlerPersistance) {
      new HarvestSolrDaemon(waitInterval, crawlers, daemonPort)
      .startCrawling();
    }
    
  }
}
