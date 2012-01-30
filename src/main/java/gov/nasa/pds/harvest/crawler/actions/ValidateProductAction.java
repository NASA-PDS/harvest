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
package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.tools.label.ExceptionContainer;
import gov.nasa.pds.tools.label.ExceptionType;
import gov.nasa.pds.tools.label.LabelException;
import gov.nasa.pds.tools.label.LabelValidator;

/**
 * Crawler action class that validates a product.
 *
 *
 * @author mcayanan
 *
 */
public class ValidateProductAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      ValidateProductAction.class.getName());

  /** Crawler action ID. */
  private final String ID = "ValidateProductAction";

  /** Crawler action description. */
  private final String DESCRIPTION = "Validates a product.";

  /** Model version to use when validating products. */
  private String modelVersion;

  /**
   * Constructor.
   *
   */
  public ValidateProductAction(String modelVersion) {
    super();
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
    this.modelVersion = modelVersion;
  }

  /**
   * Perform the action to validate a product.
   *
   * @param product The product to validate.
   * @param productMetadata The metadata associated with the given product.
   *
   * @return true if the product validated successfully.
   *
   * @throws CrawlerActionException Not used at the moment.
   */
  @Override
  public boolean performAction(File product, Metadata productMetadata)
  throws CrawlerActionException {
    boolean passFlag = true;
    LabelValidator lv = new LabelValidator();
    ExceptionContainer exceptionContainer = new ExceptionContainer();
    try {
      lv.setModelVersion(modelVersion);
      lv.validate(exceptionContainer, product);
      exceptionContainer.getExceptions();
    } catch(Exception e) {
      LabelException le = new LabelException(ExceptionType.FATAL,
          e.getMessage(), product.toString(), product.toString(), null, null);
      exceptionContainer.addException(le);
    }
    if (exceptionContainer.hasError() || exceptionContainer.hasFatal()) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
              "Product did not pass validation.", product));
      passFlag = false;
    }
    for (LabelException le : exceptionContainer.getExceptions()) {
      record(le);
    }
    return passFlag;
  }

  /**
   * Record the label exception.
   *
   * @param exception The label exception.
   */
  private void record(LabelException exception) {
    Level level = null;
    if (ExceptionType.WARNING.equals(exception.getExceptionType())) {
      level = Level.WARNING;
    } else if (ExceptionType.ERROR.equals(exception.getExceptionType())
          || (ExceptionType.FATAL.equals(exception.getExceptionType()))
          ) {
      level = Level.SEVERE;
    } else {
      level = Level.INFO;
    }
    log.log(new ToolsLogRecord(level, exception.getMessage(),
        exception.getSource(), exception.getLineNumber()));
  }
}
