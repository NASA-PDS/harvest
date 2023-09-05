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

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
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
