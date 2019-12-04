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

import java.util.ArrayList;
import java.util.List;

import org.apache.tika.Tika;

/**
 * Class that contains file information to be used in registering file objects
 * to the PDS4 Registry.
 *
 * @author mcayanan
 *
 */
public class FileObject {
  /** File name. */
  private String name;

  /** File location. */
  private String location;

  /** File size. */
  private FileSize size;

  /** File creation date time. */
  private String creationDateTime;

  /** md5 checksum. */
  private String checksum;

  /** The product identifier when registered to the PDS Storage Service. */
  private String storageServiceProductId;

  /** Access urls to the file object. */
  private List<String> accessUrls;

  private String mimeType;

  private String fileType;
  
  /**
   * Constructor.
   *
   * @param name File name.
   * @param location File location.
   * @param size File size.
   * @param creationDateTime File creation date time.
   * @param checksum checksum of the file.
   */
  public FileObject(String name, String location, FileSize size,
      String creationDateTime, String checksum, String fileType) {
    this.name = name;
    this.location = location;
    this.size = size;
    this.creationDateTime = creationDateTime;
    this.checksum = checksum;
    this.storageServiceProductId = null;
    this.accessUrls = new ArrayList<String>();
    this.fileType = fileType;
    this.mimeType = new Tika().detect(name);
  }

  public String getName() {return name;}

  public String getLocation() {return location;}

  public FileSize getSize() {return size;}

  public String getCreationDateTime() {return creationDateTime;}

  public String getChecksum() {return checksum;}

  public String getMimeType() {return mimeType;}

  public String getFileType() {return fileType;}

  public void setStorageServiceProductId(String productId) {
    this.storageServiceProductId = productId;
  }

  public String getStorageServiceProductId() {
    return storageServiceProductId;
  }

  public void setAccessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
  }

  public List<String> getAccessUrls() {
    return accessUrls;
  }
}
