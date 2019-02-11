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
package gov.nasa.pds.harvest.search.crawler.actions;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;

/**
 * Pre-ingest Crawler Action that checks to see that the title value is
 * less than 255 characters.
 *
 * @author mcayanan
 *
 */
public class TitleLengthCheckerAction extends CrawlerAction {

  /** Logger object. */
  private static Logger log = Logger.getLogger(
      TitleLengthCheckerAction.class.getName());

  /** Crawler action id. */
  private final static String ID = "TitleLengthCheckerAction";

  /** Crawler action description. */
  private final static String DESCRIPTION = "Checks to see that the title "
      + "value does not exceed 255 characters.";

  /**
   * Constructor.
   */
  public TitleLengthCheckerAction() {
    super();
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
  }

  /**
   * Performs the crawler action that verifies that the title value
   * is less than 255 characters.
   *
   * @param product The product file.
   * @param metadata The product metadata.
   *
   * @throws CrawlerActionException None thrown.
   *
   */
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    boolean passFlag = true;
    if (metadata.containsKey(Constants.TITLE)) {
      if (metadata.getMetadata(Constants.TITLE).length()
          > Constants.TITLE_MAX_LENGTH) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            "Title metadata value exceeds " + Constants.TITLE_MAX_LENGTH
            + " characters: "
                + metadata.getMetadata(Constants.TITLE), product));
        passFlag = false;
      }
    }
    return passFlag;
  }


}
