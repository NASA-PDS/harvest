// Copyright 2006-2012, by the California Institute of Technology.
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

package gov.nasa.pds.harvest.file;

import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * Class that reads a checksum manifest file.
 *
 * @author mcayanan
 *
 */
public class ChecksumManifest {
  /** logger object. */
  private static Logger log = Logger.getLogger(
      ChecksumManifest.class.getName());

  /**
   * Reads a checksum manifest file.
   *
   * @param manifest The checksum manifest.
   *
   * @return A hash map of absolute file pathnames to checksum values.
   *
   * @throws IOException If there was an error reading the checksum manifest.
   */
  public static HashMap<File, String> read(File manifest)
  throws IOException {
    HashMap<File, String> checksums = new HashMap<File, String>();
    String parent = manifest.getParent();
    LineNumberReader reader = new LineNumberReader(new FileReader(manifest));
    String line = "";
    try {
      log.log(new ToolsLogRecord(ToolsLevel.INFO,
          "Processing checksum manifest.", manifest));
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.equals("")) {
          continue;
        }
        String[] tokens = line.split("\\s{1,2}", 2);
        File file = new File(tokens[1]);
        if (!file.isAbsolute()) {
          file = new File(parent, file.toString());
        }
        //Normalize the file
        file = new File(FilenameUtils.normalize(file.toString()));
        checksums.put(file, tokens[0]);
        log.log(new ToolsLogRecord(ToolsLevel.DEBUG, "Map contains file '"
            + file.toString() + "' with checksum of '"
            + tokens[0] + "'.", manifest));
      }
    } catch (ArrayIndexOutOfBoundsException ae) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Could not tokenize: "
          + line, manifest.toString(), reader.getLineNumber()));
      throw new IOException(ae.getMessage());
    } finally {
      reader.close();
    }
    return checksums;
  }
}
