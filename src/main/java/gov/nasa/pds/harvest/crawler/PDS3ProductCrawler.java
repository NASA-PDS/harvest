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
package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.crawler.metadata.extractor.Pds3MetExtractor;
import gov.nasa.pds.harvest.crawler.metadata.extractor.Pds3MetExtractorConfig;
import gov.nasa.pds.harvest.crawler.status.Status;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.ManualPathResolver;
import gov.nasa.pds.tools.label.parser.DefaultLabelParser;
import gov.nasa.pds.tools.util.MessageUtils;

/**
 * Class to crawl PDS3 data products.
 *
 * @author mcayanan
 *
 */
public class PDS3ProductCrawler extends PDSProductCrawler {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          PDS3ProductCrawler.class.getName());

  /** The configuration object for PDS3 product metadata registrations. */
  private Pds3MetExtractorConfig config;

  /** Gets the PDS3 metextractor configuration object.
   *
   * @return Return the configuration object.
   */
  public Pds3MetExtractorConfig getPDS3MetExtractorConfig() {
    return this.config;
  }

  /** Sets the PDS3 metextractor configuration.
   *
   *  @param config A configuration object.
   */
  public void setPDS3MetExtractorConfig(Pds3MetExtractorConfig config) {
    this.config = config;
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
      Pds3MetExtractor metExtractor = new Pds3MetExtractor(config);
      try {
          return metExtractor.extractMetadata(product);
      } catch (MetExtractionException m) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
                  "Error while gathering metadata: " + m.getMessage(),
                  product));
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
    log.log(new ToolsLogRecord(ToolsLevel.INFO, "Begin processing.",
        product));
    boolean passFlag = true;
    ManualPathResolver resolver = new ManualPathResolver();
    resolver.setBaseURI(ManualPathResolver.getBaseURI(product.toURI()));
    DefaultLabelParser parser = new DefaultLabelParser(false, false, resolver);
    Label label = null;
    try {
      label = parser.parseLabel(product.toURI().toURL());
    } catch (LabelParserException lp) {
      passFlag = false;
      ++numFilesSkipped;
      log.log(new ToolsLogRecord(ToolsLevel.SKIP,
          MessageUtils.getProblemMessage(lp), product));
    } catch (Exception e) {
      passFlag = false;
      ++numFilesSkipped;
      log.log(new ToolsLogRecord(ToolsLevel.SKIP, e.getMessage(), product));
    }
    if (passFlag == true) {
      log.log(new ToolsLogRecord(ToolsLevel.NOTIFICATION, Status.DISCOVERY,
        product));
      ++numDiscoveredProducts;
    }
    return passFlag;



    //TODO: This reports problems in a PDS3 label. Can we suppress this?
/*
    if (!label.getProblems().isEmpty()) {
      passFlag = false;
      for (LabelParserException problem : label.getProblems()) {
        report(problem, product);
      }
    }
*/
  }

  private void report(LabelParserException problem, File product) {
    String message = MessageUtils.getProblemMessage(problem);
    if ("INFO".equalsIgnoreCase(problem.getType().getSeverity().getName())) {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, message,
          product.toString(),problem.getLineNumber()));
    } else if ("WARNING".equalsIgnoreCase(
        problem.getType().getSeverity().getName())) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING, message,
          product.toString(),problem.getLineNumber()));
    } else {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, message,
          product.toString(),problem.getLineNumber()));
    }
  }
}
