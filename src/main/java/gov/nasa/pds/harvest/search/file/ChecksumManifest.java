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

package gov.nasa.pds.harvest.search.file;

import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;

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

  private File basePath;
  
  /**
   * Constructor.
   * 
   * @param basePath A base path for resolving relative file references.
   */
  public ChecksumManifest(String basePath) {
    this.basePath = new File(basePath);
  }
  
  /**
   * Reads a checksum manifest file.
   *
   * @param manifest The checksum manifest.
   *
   * @return A hash map of absolute file pathnames to checksum values.
   *
   * @throws IOException If there was an error reading the checksum manifest.
   */
  public HashMap<File, String> read(File manifest)
  throws IOException {
    HashMap<File, String> checksums = new HashMap<File, String>();
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
        file = new File(FilenameUtils.normalize(file.toString()));
        if (!file.isAbsolute()) {
          file = new File(basePath, file.toString());
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
