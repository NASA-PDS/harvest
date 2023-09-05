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

import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionRepo;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.crawler.actions.LidCheckerAction;
import gov.nasa.pds.harvest.search.crawler.actions.LogMissingReqMetadataAction;
import gov.nasa.pds.harvest.search.crawler.actions.TitleLengthCheckerAction;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.BundleMetExtractor;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.CollectionMetExtractor;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds4MetExtractor;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds4MetExtractorConfig;
import gov.nasa.pds.harvest.search.doc.SearchDocState;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.DirectoryFilter;
import gov.nasa.pds.harvest.search.policy.FileFilter;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;
import gov.nasa.pds.harvest.search.util.LidVid;
import gov.nasa.pds.harvest.search.util.XMLExtractor;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.saxon.trans.XPathException;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.xml.sax.SAXParseException;

/**
 * Class that extends the Cas-Crawler to crawl a directory or
 * PDS inventory file and register products to the PDS Registry
 * Service.
 *
 * @author mcayanan
 *
 */
public class PDSProductCrawler extends ProductCrawler {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      PDSProductCrawler.class.getName());

  /** Holds the configuration to extract metadata. */
  private Pds4MetExtractorConfig metExtractorConfig;

  /** A list of crawler actions to perform while crawling. */
  private List<String> crawlerActions;

  /** Holds the product type of the file being processed. */
  private String objectType;

  /** Flag for crawler persistance. */
  protected boolean inPersistanceMode;

  /** A map of files that were touched during crawler persistance. */
  protected Map<File, Long> touchedFiles;

  private SearchDocState searchDocState;
  
  /**
   * Default constructor.
   *
   */
  public PDSProductCrawler() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param extractorConfig A configuration class that tells the crawler
   * what data product types to look for and what metadata to extract.
   */
  public PDSProductCrawler(Pds4MetExtractorConfig extractorConfig) {
    this.objectType = "";
    this.metExtractorConfig = extractorConfig;
    this.crawlerActions = new ArrayList<String>();
    inPersistanceMode = false;
    touchedFiles = new HashMap<File, Long>();

    String[] reqMetadata = {
        Constants.PRODUCT_VERSION,
        Constants.LOGICAL_ID,
        Constants.OBJECT_TYPE,
        };
    setRequiredMetadata(Arrays.asList(reqMetadata));
    List<IOFileFilter> fileFilters = new ArrayList<IOFileFilter>();
    fileFilters.add(FileFilterUtils.fileFileFilter());
    fileFilters.add(new WildcardOSFilter("*"));
    FILE_FILTER = new AndFileFilter(fileFilters);
    crawlerActions.add(new LogMissingReqMetadataAction(getRequiredMetadata()).getId());
    crawlerActions.add(new LidCheckerAction().getId());
    crawlerActions.add(new TitleLengthCheckerAction().getId());
  }

  /**
   * Get the MetExtractor configuration object.
   *
   * @return The PDSMetExtractorConfig object.
   */
  public Pds4MetExtractorConfig getMetExtractorConfig() {
    return metExtractorConfig;
  }

  public void setMetExtractorConfig(Pds4MetExtractorConfig config) {
    this.metExtractorConfig = config;
  }

  public void setInPersistanceMode(boolean value) {
    inPersistanceMode = value;
  }

  /**
   * Sets the file filter for the crawler.
   *
   * @param filter A File Filter defined in the Harvest policy config.
   */
  public void setFileFilter(FileFilter filter) {
    List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
    filters.add(FileFilterUtils.fileFileFilter());
    if (filter != null && !filter.getInclude().isEmpty()) {
      filters.add(new WildcardOSFilter(filter.getInclude()));
    } else if (filter != null && !filter.getExclude().isEmpty()) {
      filters.add(new NotFileFilter(new WildcardOSFilter(
          filter.getExclude())));
    }
    FILE_FILTER = new AndFileFilter(filters);
  }

  /**
   * Sets the directory filter for the crawler.
   *
   * @param filter A Directory Filter defined in the Harvest policy config.
   */
  public void setDirectoryFilter(DirectoryFilter filter) {
    if (!filter.getExclude().isEmpty()) {
      List<IOFileFilter> dirFilters = new ArrayList<IOFileFilter>();
      dirFilters.add(FileFilterUtils.directoryFileFilter());
      dirFilters.add(new NotFileFilter(new WildcardOSFilter(
          filter.getExclude())));
      DIR_FILTER = new AndFileFilter(dirFilters);
    }
  }

  /**
   * Method not implemented at the moment.
   *
   * @param product The product file.
   * @param productMetadata The metadata associated with the product.
   */
//  @Override
//  protected void addKnownMetadata(File product, Metadata productMetadata) {
    //The parent class adds FILENAME, FILE_LOCATION, and PRODUCT_NAME
    //to the metadata. Not needed at the moment
//  }

  /**
   * Crawls the given directory.
   *
   * @param dir The directory to crawl.
   */
  public void crawl(File dir) {
    //Load crawlerActions first before crawling
    CrawlerActionRepo repo = new CrawlerActionRepo();
    repo.loadActionsFromBeanFactory(getApplicationContext(), crawlerActions);
    repo.getActions();
    try {
      super.crawl(dir);
    } catch (IllegalArgumentException ie) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, ie.getMessage()));
    }
  }

  /**
   * Adds a crawler action.
   *
   * @param action A crawler action.
   */
  public void addAction(CrawlerAction action) {
    this.crawlerActions.add(action.getId());
  }

  /**
   * Adds a list of crawler actions.
   *
   * @param actions A list of crawler actions.
   */
  public void addActions(List<CrawlerAction> actions) {
	  for (CrawlerAction action : actions) {
		  this.crawlerActions.add(action.getId());
  	}
  }

  /**
   * Gets a list of crawler actions defined for the crawler.
   *
   * @return A list of crawler actions that will be performed
   * during crawling.
   */
  public List<String> getActions() {
    return crawlerActions;
  }

  /**
   * Extracts metadata from the given product.
   *
   * @param product A PDS file.
   *
   * @return A Metadata object, which holds metadata from the product.
   *
   */
  @Override
  protected Metadata getMetadataForProduct(File product) {
    Pds4MetExtractor metExtractor = null;
    if (objectType.equalsIgnoreCase(Constants.BUNDLE)) {
      metExtractor = new BundleMetExtractor(metExtractorConfig);
    } else if (objectType.equalsIgnoreCase(Constants.COLLECTION)) {
      metExtractor = new CollectionMetExtractor(metExtractorConfig);
    } else {
      metExtractor = new Pds4MetExtractor(metExtractorConfig);
    }
    try {
      return metExtractor.extractMetadata(product);
    } catch (MetExtractionException m) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
          "Error while gathering metadata: " + m.getMessage(), product));
      return new Metadata();
    }
  }

  /**
   * Determines whether the supplied file passes the necessary
   * pre-conditions for the file to be registered.
   *
   * @param product A file.
   *
   * @return true if the file passes.
   */
  @Override
  protected boolean passesPreconditions(File product) {
    if (inPersistanceMode) {
      if (touchedFiles.containsKey(product)) {
        long lastModified = touchedFiles.get(product);
        if (product.lastModified() == lastModified) {
          return false;
        } else {
          touchedFiles.put(product, product.lastModified());
        }
      } else {
        touchedFiles.put(product, product.lastModified());
      }
    }
    if (Constants.collections.contains(product)) {
      return false;
    }
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Begin processing.", product));
    boolean passFlag = true;
    objectType = "";
    XMLExtractor extractor = new XMLExtractor();
    try {
      extractor.parse(product);
    } catch (XPathException xe) {
      if (xe.getException() instanceof SAXParseException) {
        SAXParseException spe = (SAXParseException) xe.getException();
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, spe.getMessage(),
            product.toString(), spe.getLineNumber()));
      } else {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Parse failure: "
            + xe.getMessage(), product));
      }
      passFlag = false;
    }
    if (passFlag == false) {
      ++HarvestSolrStats.numBadFiles;
      return false;
    } else  {
      try {
        String lid = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
          Constants.LOGICAL_ID));
        String version = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
            Constants.PRODUCT_VERSION));
        // Check to see if the product is part of the non-primary member list.
        int index = Constants.nonPrimaryMembers.indexOf(new LidVid(lid));
        if (index != -1) {
          LidVid lidvid = Constants.nonPrimaryMembers.get(index);
          if (lidvid.hasVersion()) {
            if (lidvid.getVersion().equals(version)) {
              log.log(new ToolsLogRecord(ToolsLevel.SKIP,
                "Not a primary member.", product));
              ++HarvestSolrStats.numFilesSkipped;
              return false;
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.SKIP,
                "Not a primary member.", product));
              ++HarvestSolrStats.numFilesSkipped;
              return false;
          }
        }
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Problem extracting "
            + "LIDVID: " + e.getMessage(), product));
        ++HarvestSolrStats.numBadFiles;
        return false;
      }
      try {
        objectType = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
            Constants.PRODUCT_CLASS));
        if ("".equals(objectType)) {
          log.log(new ToolsLogRecord(ToolsLevel.SKIP, "No "
              + Constants.PRODUCT_CLASS + " element found.", product));
          ++HarvestSolrStats.numFilesSkipped;
          passFlag = false;
        } else if (metExtractorConfig.hasObjectType(objectType)) {
          ++HarvestSolrStats.numGoodFiles;
          passFlag = true;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.SKIP,
              "\'" + objectType + "\' is not an object type" +
              " found in the policy file.", product));
          ++HarvestSolrStats.numFilesSkipped;
          passFlag = false;
        }
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Problem getting '"
            + Constants.PRODUCT_CLASS + "': " + e.getMessage(), product));
        ++HarvestSolrStats.numBadFiles;
        return false;
      }
    }

    return passFlag;
  }
  
  /**
   * Sets the Search Service URL location.
   *
   * @param url A url of the Search Service location.
   * @throws MalformedURLException If the given url is malformed.
   */
  public void setSearchUrl(String url) throws MalformedURLException {
    setFilemgrUrl(url);
  }
  
  public void setCounter(SearchDocState searchDocState) {
    this.searchDocState = searchDocState;
  }

	@Override
	protected File renameProduct(File product, Metadata productMetadata) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
