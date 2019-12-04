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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * A class that calculates the MD5 checksum of a file.
 *
 * @author mcayanan
 *
 */
public class MD5Checksum {

  /** HEX values. */
  private static final String HEXES = "0123456789abcdef";


  /**
   * Gets the MD5 checksum value.
   *
   * @param filename The filename.
   * @return The MD5 checksum of the given filename.
   *
   * @throws Exception If an error occurred while calculating the checksum.
   */
  public static String getMD5Checksum(String filename) throws Exception {
    byte[] b = createChecksum(filename);
    return getHex(b);
  }

  /**
   * Creates the checksum.
   *
   * @param filename The filename.
   *
   * @return a byte array of the checksum.
   *
   * @throws Exception If an error occurred while calculating the checksum.
   */
  private static byte[] createChecksum(String filename) throws Exception {
    InputStream input = null;
    try {
      input =  new FileInputStream(filename);
      byte[] buffer = new byte[1024];
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      int bytesRead = 0;
      do {
        bytesRead = input.read(buffer);
        if (bytesRead > 0) {
          md5.update(buffer, 0, bytesRead);
        }
      } while (bytesRead != -1);
      return md5.digest();
    } finally {
      input.close();
    }
  }

  /**
   * Gets the HEX equivalent of the given byte array.
   *
   * @param bytes The bytes to convert.
   *
   * @return The HEX value of the given byte array.
   */
  private static String getHex(byte [] bytes) {
    if (bytes == null) {
      return null;
    }
    final StringBuilder hex = new StringBuilder(2 * bytes.length);
    for (byte b : bytes ) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
      .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }
}
