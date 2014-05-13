// Copyright 2006-2014, by the California Institute of Technology.
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

/**
 * Class representation of being able to specify units along with a file size.
 * 
 * @author mcayanan
 *
 */
public class FileSize {
  private long size;
  private String units;
  
  /**
   * Constructor.
   * 
   * @param size The file size.
   * @param units A units designation.
   */
  public FileSize(long size, String units) {
    this.size = size;
    this.units = units;
  }
  
  /**
   * @return the size.
   */
  public long getSize() {
    return this.size;
  }
  
  /**
   * @return the units.
   */
  public String getUnits() {
    return this.units;
  }
  
  /**
   * @return Determines whether a units value was defined.
   */
  public boolean hasUnits() {
    if (!this.units.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }
}
