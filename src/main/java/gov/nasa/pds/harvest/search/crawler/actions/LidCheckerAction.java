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
 * Pre-ingest Crawler Action that checks to see if the logical identifier
 * of a PDS4 data product contains URN reserved and/or excluded characters.
 *
 * @author mcayanan
 *
 */
public class LidCheckerAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          CreateAccessUrlsAction.class.getName());

  /** The list of URN reserved and/or excluded characters. */
  private final static String URN_ILLEGAL_CHARACTERS = "[^%/\\\\?#\"&<>\\[\\]\\^`\\{\\|\\}~]*";

  /** Crawler action id. */
  private final static String ID = "LidCheckerAction";

  /** Crawler action description. */
  private final static String DESCRIPTION = "Checks to see if the logical "
    + "identifier contains urn reserved and/or excluded characters.";

  /**
   * Constructor.
   *
   */
  public LidCheckerAction() {
    super();
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
  }

  /**
   * Performs the crawler action that looks for URN reserved and
   *  excluded characters within a lid.
   *
   * @param product The product file.
   * @param metadata The product metadata.
   *
   * @return true if there are no URN reserved and/or excluded characters
   *  in the lid.
   *
   *  @throws CrawlerActionException None thrown.
   */
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    boolean passFlag = true;
    if (metadata.containsKey(Constants.LOGICAL_ID)) {
      String lid = metadata.getMetadata(Constants.LOGICAL_ID);
      if (!lid.matches(URN_ILLEGAL_CHARACTERS)) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Lid contains URN "
            + "reserved and/or excluded characters: " + lid, product));
        passFlag = false;
      }
      for (String badEnding : Arrays.asList(new String[]{".xml", ".json"})) {
        if (lid.endsWith(badEnding)) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
              "Lid cannot end in '" + badEnding + "': " + lid, product));
          passFlag = false;
        }
      }
    }
    return passFlag;
  }
}
