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

import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionPhases;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
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
    String []phases = {CrawlerActionPhases.PRE_INGEST.getName()};
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
