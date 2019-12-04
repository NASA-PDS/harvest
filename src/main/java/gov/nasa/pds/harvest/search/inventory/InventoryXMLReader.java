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

package gov.nasa.pds.harvest.search.inventory;

import java.io.File;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.util.XMLExtractor;
import net.sf.saxon.tree.tiny.TinyElementImpl;

/**
 * Class that supports the reading of an XML version of the
 * PDS Inventory file.
 *
 * @author mcayanan
 *
 */
public class InventoryXMLReader implements InventoryReader {
  /** The directory path of the Inventory file. */
  private String parentDirectory;

  /** An index to keep track of the number of inventory entries. */
  private int index;

  /** A list of nodes containing the inventory entries. */
  private List<TinyElementImpl> memberEntries;

  /** The XML Extractor */
  private XMLExtractor extractor;

  /**
   * Constructor.
   *
   * @param file A PDS Inventory file
   *
   * @throws InventoryReaderException If an error occurred while reading
   * the Inventory file.
   */
  public InventoryXMLReader(File file)
  throws InventoryReaderException {
    index = 0;
    parentDirectory = file.getParent();
    extractor = new XMLExtractor();
    try {
      extractor.parse(file);
      memberEntries = extractor.getNodesFromDoc(Constants.coreXpathsMap.get(
          Constants.REFERENCES));
    } catch (Exception e) {
      throw new InventoryReaderException(e);
    }
  }

  /**
   * Gets the next product file reference in the PDS Inventory file.
   *
   * @return A class representation of the next product file reference
   * in the PDS inventory file. If the end-of-file has been reached,
   * a null value will be returned.
   *
   * @throws InventoryReaderException If an error occurred while reading
   * the Inventory file.
   *
   */
  public InventoryEntry getNext() throws InventoryReaderException {
    if (index >= memberEntries.size()) {
      return null;
    }

    TinyElementImpl entry = memberEntries.get(index++);
    String lidvid = "";
    String memberStatus = "";
    try {
      lidvid = extractor.getValueFromItem(
          InventoryKeys.IDENTITY_REFERENCE_XPATH, entry);
      memberStatus = extractor.getValueFromItem(
          InventoryKeys.MEMBER_STATUS_XPATH, entry);
    } catch (XPathExpressionException x) {
      throw new InventoryReaderException(x);
    }
    return new InventoryEntry(lidvid, memberStatus);
  }
}
