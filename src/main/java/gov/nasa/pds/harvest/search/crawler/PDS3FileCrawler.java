// Copyright 2006-2016, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.search.crawler;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.crawler.metadata.extractor.Pds3FileMetExtractor;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.FileFilter;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

/**
 * Crawler class intended to be used for registering PDS3 files as
 * Product_File_Repository products.
 *
 * @author mcayanan
 *
 */
public class PDS3FileCrawler extends PDS3ProductCrawler {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      PDS3FileCrawler.class.getName());

  /** Flag to enable generation of checksums on the fly. */
  private boolean generateChecksums;

  /** Represents the checksum manifest file. */
  private Map<File, String> checksumManifest;

  public PDS3FileCrawler() {
    generateChecksums = false;
    checksumManifest = new HashMap<File, String>();
    List<IOFileFilter> fileFilters = new ArrayList<IOFileFilter>();
    fileFilters.add(FileFilterUtils.fileFileFilter());
    fileFilters.add(new NotFileFilter(new WildcardOSFilter("*.LBL")));
    FILE_FILTER = new AndFileFilter(fileFilters);
  }

  public void setFileFilter(FileFilter filter) {
    List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
    filters.add(FileFilterUtils.fileFileFilter());
    if (filter != null && !filter.getInclude().isEmpty()) {
      filters.add(new WildcardOSFilter(filter.getInclude()));
    } else if (filter != null && !filter.getExclude().isEmpty()) {
      filters.add(new NotFileFilter(new WildcardOSFilter(
          filter.getExclude())));
    }
    filters.add(new NotFileFilter(new WildcardOSFilter("*.LBL")));
    FILE_FILTER = new AndFileFilter(filters);
  }

  protected Metadata getMetadataForProduct(File product) {
    Pds3FileMetExtractor metExtractor = new Pds3FileMetExtractor(
        getPDS3MetExtractorConfig());
    metExtractor.setChecksumManifest(checksumManifest);
    metExtractor.setGenerateChecksums(generateChecksums);
    try {
        return metExtractor.extractMetadata(product);
    } catch (MetExtractionException m) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
                "Error while gathering metadata: " + m.getMessage(),
                product));
        return new Metadata();
    }
  }

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
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Begin processing.",
        product));
    boolean passFlag = true;
    try {
      if (!product.canRead()) {
        passFlag = false;
        ++HarvestSolrStats.numFilesSkipped;
      } else {
        // Check for associated label file
        String labelFileName = FilenameUtils.getBaseName((product.getName())) + ".LBL";
        File label = new File(product.getParent(), labelFileName);
        if (label.exists()) {
          ++HarvestSolrStats.numFilesSkipped;
          log.log(new ToolsLogRecord(ToolsLevel.SKIP,
              "An associated label file exists '" + label.toString() + "'", product));
          passFlag = false;
        } else {
          ++HarvestSolrStats.numGoodFiles;
        }
      }
    } catch (SecurityException se) {
      passFlag = false;
      ++HarvestSolrStats.numFilesSkipped;
      log.log(new ToolsLogRecord(ToolsLevel.SKIP,
          se.getMessage(), product));
    }
    return passFlag;
  }

  /**
   * Set the flag for checksum generation.
   *
   * @param value 'true' to turn on, 'false' to turn off.
   */
  public void setGenerateChecksums(boolean value) {
    this.generateChecksums = value;
  }

  /**
   * Set the map to represent the checksum manifest file.
   *
   * @param manifest A mapping of file objects to checksums.
   */
  public void setChecksumManifest(Map<File, String> manifest) {
    this.checksumManifest = manifest;
  }
}
