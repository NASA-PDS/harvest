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
package gov.nasa.pds.harvest.logging.formatter;

import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Class that formats the Harvest logging messages.
 *
 * @author mcayanan
 *
 */
public class HarvestFormatter extends Formatter {
  private static String lineFeed = System.getProperty("line.separator", "\n");
  private static String doubleLineFeed = lineFeed + lineFeed;

  private StringBuffer config;
  private StringBuffer summary;

  private int numWarnings;

  private int numErrors;

  public HarvestFormatter() {
    config = new StringBuffer("PDS Harvest Tool Log" + doubleLineFeed);
    summary = new StringBuffer("Summary:" + doubleLineFeed);
    numWarnings = 0;
    numErrors = 0;
  }

  public String format(LogRecord record) {
    if (record instanceof ToolsLogRecord) {
      ToolsLogRecord tlr = (ToolsLogRecord) record;
      StringBuffer message = new StringBuffer();
      if (tlr.getLevel().intValue() == ToolsLevel.NOTIFICATION.intValue()) {
        return tlr.getMessage() + lineFeed;
      }
      if (tlr.getLevel().intValue() == ToolsLevel.WARNING.intValue()) {
        ++numWarnings;
        ++HarvestStats.numWarnings;
      } else if (tlr.getLevel().intValue() == ToolsLevel.SEVERE.intValue()) {
        ++numErrors;
        ++HarvestStats.numErrors;
      }
      if (tlr.getLevel().intValue() != ToolsLevel.CONFIGURATION.intValue()) {
        if (tlr.getLevel().intValue() == ToolsLevel.SEVERE.intValue()) {
          message.append("ERROR");
        } else {
          message.append(tlr.getLevel().getName());
        }
        message.append(":   ");
      }
      if (tlr.getFilename() != null) {
        message.append("[" + tlr.getFilename() + "] ");
      }
      if (tlr.getLine() != -1) {
        message.append("line " + tlr.getLine() + ": ");
      }
      message.append(tlr.getMessage());
      message.append(lineFeed);

      return message.toString();
    } else {
      return "******* " + record.getMessage() + " ************" + lineFeed;
    }
  }

  private void processSummary() {
    int totalFiles = HarvestStats.numGoodFiles + HarvestStats.numBadFiles;
    int totalAssociations = HarvestStats.numAssociationsRegistered
    + HarvestStats.numAssociationsNotRegistered;

    int totalProducts = HarvestStats.numProductsRegistered
    + HarvestStats.numProductsNotRegistered;

    int totalAncillaryProducts = HarvestStats.numAncillaryProductsRegistered
    + HarvestStats.numAncillaryProductsNotRegistered;

    summary.append(HarvestStats.numGoodFiles + " of " + totalFiles
        + " file(s) processed, " + HarvestStats.numFilesSkipped
        + " other file(s) skipped" + lineFeed);
    summary.append(numErrors + " error(s), " + numWarnings + " warning(s)"
        + doubleLineFeed);
    summary.append(HarvestStats.numProductsRegistered + " of " + totalProducts
        + " products registered." + lineFeed);
    summary.append(HarvestStats.numAncillaryProductsRegistered + " of "
        + totalAncillaryProducts + " ancillary products registered."
        + doubleLineFeed);
    summary.append("Product Types Registered:" + lineFeed);
    for (Entry<String, List<File>> entry :
      HarvestStats.registeredProductTypes.entrySet()) {
      summary.append(entry.getValue().size() + " " + entry.getKey()
          + lineFeed);
    }

    int totalGeneratedChecksums = HarvestStats.numChecksumsSame
    + HarvestStats.numChecksumsDifferent;

    summary.append(lineFeed + HarvestStats.numChecksumsSame + " of "
        + totalGeneratedChecksums + " generated checksums matched "
        + "their supplied value, " + HarvestStats.numChecksumsNotChecked
        + " generated value(s) not checked." + lineFeed);

    summary.append(lineFeed + HarvestStats.numAssociationsRegistered
        + " of " + totalAssociations + " associations registered."
        + lineFeed);
  }

  public String getTail(Handler handler) {
    StringBuffer report = new StringBuffer("");

    processSummary();

    report.append(lineFeed);
    report.append(summary);
    report.append(doubleLineFeed + "End of Log" + doubleLineFeed);

    return report.toString();
  }
}
