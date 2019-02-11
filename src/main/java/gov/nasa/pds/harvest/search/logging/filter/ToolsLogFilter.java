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
package gov.nasa.pds.harvest.search.logging.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Class to filter logging messages that are coming from the underlying
 * framework.
 *
 * @author mcayanan
 *
 */
public class ToolsLogFilter implements Filter {

    /**
     * Method that checks if a log record is loggable.
     *
     * @param record The LogRecord.
     *
     * @return true if the record can be logged by the handler.
     *
     */
    @Override
    public boolean isLoggable(LogRecord record) {
        String casCrawlerName = "gov.nasa.jpl.oodt.cas.crawl";
        String jerseyName = "com.sun.jersey.core";
        String fileMgrVersionerName = "org.apache.oodt.cas.filemgr.versioning";
        String inPlacedataTransferName =
          "org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferer";
        if ((record.getLoggerName() != null)
                && (record.getLoggerName().contains(casCrawlerName)
                    || record.getLoggerName().contains(jerseyName)
                    || record.getLoggerName().contains(fileMgrVersionerName)
                    || record.getLoggerName().contains(inPlacedataTransferName)
                    )) {
            return false;
        } else {
            return true;
        }
    }
}
