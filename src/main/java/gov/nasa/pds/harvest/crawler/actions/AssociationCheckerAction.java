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
import java.util.List;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;

/**
 * Class to check the validity of an association entry in a product.
 *
 * @author mcayanan
 *
 */
public class AssociationCheckerAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          AssociationCheckerAction.class.getName());

  /** Crawler action ID. */
  private final String ID = "AssociationCheckerAction";

  /** Crawler action description. */
  private final String DESCRIPTION = "Checks the validity of an association.";

  /**
   * Constructor.
   *
   */
  public AssociationCheckerAction() {
      super();
      String []phases = {CrawlerActionPhases.PRE_INGEST};
      setPhases(Arrays.asList(phases));
      setId(ID);
      setDescription(DESCRIPTION);
  }

  /**
   * Perform the action.
   *
   * @param product The product associated with the association entries.
   * @param metadata The metadata associated with the given product.
   *
   * @return Method returns false if any of the association entries are
   * missing a LID or LID-VID reference or the reference_association_type
   * tag.
   */
  @Override
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    boolean passFlag = true;

    if (metadata.containsKey(Constants.REFERENCES)) {
      for (ReferenceEntry re : (List<ReferenceEntry>)
          metadata.getAllMetadata(Constants.REFERENCES)) {
        if (re.getLogicalID() == null) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Association "
              + "entry is missing a LID or LID-VID reference tag.",
              re.getFile().toString(), re.getLineNumber()));
          passFlag = false;
        }
        if (re.getType() == null) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Association "
              + "entry is missing the reference_type tag.",
              re.getFile().toString(), re.getLineNumber()));
          passFlag = false;
        }
      }
    }
    return passFlag;
  }
}
