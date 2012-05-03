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
package gov.nasa.pds.harvest;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.association.AssociationPublisher;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.crawler.BundleCrawler;
import gov.nasa.pds.harvest.crawler.CollectionCrawler;
import gov.nasa.pds.harvest.crawler.PDS3ProductCrawler;
import gov.nasa.pds.harvest.crawler.PDSProductCrawler;
import gov.nasa.pds.harvest.crawler.actions.CreateAccessUrlsAction;
import gov.nasa.pds.harvest.crawler.actions.FileObjectRegistrationAction;
import gov.nasa.pds.harvest.crawler.actions.StorageIngestAction;
import gov.nasa.pds.harvest.crawler.actions.SaveMetadataAction;
import gov.nasa.pds.harvest.crawler.daemon.HarvestDaemon;
import gov.nasa.pds.harvest.crawler.metadata.extractor.Pds3MetExtractorConfig;
import gov.nasa.pds.harvest.crawler.metadata.extractor.Pds4MetExtractorConfig;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.policy.Policy;
import gov.nasa.pds.registry.client.SecurityContext;
import gov.nasa.pds.registry.exception.RegistryClientException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;

/**
 * Front end class to the Harvest tool.
 *
 * @author mcayanan
 *
 */
public class Harvester {
  /** URL of the registry service. */
  private String registryUrl;

  /** An ingester for the PDS Registry Service. */
  private RegistryIngester ingester;

  /** The port number to use for the daemon if running Harvest in continuous
   *  mode.
   */
  private int daemonPort;

  /** The wait interval in seconds in between crawls when running Harvest in
   *  continuous mode.
   */
  private int waitInterval;

  /** Object that registers the associations. */
  private AssociationPublisher associationPublisher;

  /** CrawlerAction that performs file object registration. */
  private FileObjectRegistrationAction fileObjectRegistrationAction;

  /** The registry package GUID to associate the products being registered
   *  during a single Harvest run. */
  private String registryPackageGuid;

  /**
   * Constructor.
   *
   * @param registryUrl The registry location.
   * @param registryPackageGuid The GUID of the registry package to associate
   * to all the products being registered during a single Harvest run.
   *
   * @throws MalformedURLException
   *
   */
  public Harvester(String registryUrl, String registryPackageGuid)
  throws RegistryClientException, MalformedURLException {
    this.registryUrl = registryUrl;
    this.registryUrl = registryUrl;
    this.ingester = new RegistryIngester(registryPackageGuid);
    this.daemonPort = -1;
    this.waitInterval = -1;
    this.associationPublisher = new AssociationPublisher(this.registryUrl,
        this.ingester);
    this.fileObjectRegistrationAction = new FileObjectRegistrationAction(
        this.registryUrl, this.ingester);
    this.registryPackageGuid = registryPackageGuid;
  }

  /**
   * Sets the security.
   *
   * @param securityContext An object containing the keystore information.
   * @param username Username of an authorized user.
   * @param password Password associated with the given username.
   *
   * @throws MalformedURLException
   */
  public void setSecurity(SecurityContext securityContext, String username,
      String password) throws MalformedURLException {
    this.ingester = new RegistryIngester(registryPackageGuid,
        securityContext, username, password);
    this.associationPublisher = new AssociationPublisher(this.registryUrl,
        this.ingester);
    this.fileObjectRegistrationAction = new FileObjectRegistrationAction(
        this.registryUrl, this.ingester);
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
   * @throws RegistryClientException
   */
  private List<CrawlerAction> getDefaultCrawlerActions(Policy policy)
  throws MalformedURLException, ConnectionException {
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
    if (!policy.getAccessUrls().getBaseUrl().isEmpty()) {
      CrawlerAction cauAction = new CreateAccessUrlsAction(
          policy.getAccessUrls().getBaseUrl());
      ca.add(cauAction);
      fileObjectRegistrationActions.add(cauAction);
    }
    fileObjectRegistrationAction.setActions(fileObjectRegistrationActions);
    //This is the last action that should be performed.
    ca.add(new SaveMetadataAction());
    return ca;
  }

  /**
   * Harvest the products specified in the given policy.
   *
   * @param policy An object representation of the configuration file that
   *  specifies what to harvest.
   *
   * @throws MalformedURLException If the registry url is malformed.
   * @throws ConnectionException
   */
  public void harvest(Policy policy) throws MalformedURLException,
  ConnectionException {
    boolean doCrawlerPersistance = false;
    if (waitInterval != -1 && daemonPort != -1) {
      doCrawlerPersistance = true;
    }
    Pds4MetExtractorConfig pds4MetExtractorConfig = new Pds4MetExtractorConfig(
        policy.getCandidates().getProductMetadata());
    List<PDSProductCrawler> crawlers = new ArrayList<PDSProductCrawler>();
    // Crawl bundles
    for (String bundle : policy.getBundles().getFile()) {
      BundleCrawler bc = new BundleCrawler(pds4MetExtractorConfig);
      bc.setProductPath(bundle);
      crawlers.add(bc);
    }
    // Crawl collections
    for (String collection : policy.getCollections().getFile()) {
      CollectionCrawler cc = new CollectionCrawler(pds4MetExtractorConfig);
      cc.setProductPath(collection);
      crawlers.add(cc);
    }
    // Crawl directories
    for (String directory : policy.getDirectories().getPath()) {
      PDSProductCrawler pc = new PDSProductCrawler(pds4MetExtractorConfig);
      pc.setProductPath(directory);
      List<String> filters = policy.getDirectories().getFilePattern();
      if (!filters.isEmpty()) {
        pc.setFileFilter(filters);
      }
      crawlers.add(pc);
    }
    // Crawl a PDS3 directory
    if (policy.getPds3Directory().getPath() != null) {
      PDS3ProductCrawler p3c = new PDS3ProductCrawler();
      Pds3MetExtractorConfig pds3MetExtractorConfig =
        new Pds3MetExtractorConfig(policy.getCandidates()
            .getPds3ProductMetadata());
      p3c.setPDS3MetExtractorConfig(pds3MetExtractorConfig);
      p3c.setProductPath(policy.getPds3Directory().getPath());
      List<String> filters = policy.getPds3Directory().getFilePattern();
      if (!filters.isEmpty()) {
        p3c.setFileFilter(filters);
      }
      crawlers.add(p3c);
    }
    // Perform crawl while looping through the crawler list if
    // crawler persistance is disabled.
    for (PDSProductCrawler crawler : crawlers) {
      crawler.setRegistryUrl(registryUrl);
      crawler.setIngester(ingester);
      crawler.addActions(getDefaultCrawlerActions(policy));
      if (!doCrawlerPersistance) {
        crawler.crawl();
      }
    }
    // If crawler persistance is enabled, use the HarvestDaemon object to
    // do the crawling
    if (doCrawlerPersistance) {
      new HarvestDaemon(waitInterval, crawlers, daemonPort,
          associationPublisher, ingester).startCrawling();
    } else {
      for (Entry<File, Metadata> entry
          : Constants.registeredProducts.entrySet()) {
        associationPublisher.publish(entry.getKey(), entry.getValue());
      }
    }
  }
}
