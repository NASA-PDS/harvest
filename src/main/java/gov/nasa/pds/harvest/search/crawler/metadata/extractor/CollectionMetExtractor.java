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

package gov.nasa.pds.harvest.search.crawler.metadata.extractor;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.inventory.InventoryEntry;
import gov.nasa.pds.harvest.search.inventory.InventoryReaderException;
import gov.nasa.pds.harvest.search.inventory.InventoryTableReader;
import gov.nasa.pds.harvest.search.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.util.LidVid;
import gov.nasa.pds.registry.model.Slot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.saxon.tree.tiny.TinyElementImpl;

/**
 * Class to extract metadata from a PDS Collection file.
 *
 * @author mcayanan
 *
 */
public class CollectionMetExtractor extends Pds4MetExtractor {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      CollectionMetExtractor.class.getName());

  /** XPath to get the associaton type. */
  public static final String ASSOCIATION_TYPE_XPATH = "//*[starts-with("
      + "name(),'Inventory')]/reference_type";

  /**
   * Constructor.
   *
   * @param config The configuration for the metadata extraction.
   */
  public CollectionMetExtractor(Pds4MetExtractorConfig config) {
    super(config);
  }

  /**
   * Extract the metadata
   *
   * @param product A PDS4 collection file
   * @return a class representation of the extracted metadata
   *
   */
  public Metadata extractMetadata(File product)
  throws MetExtractionException {
    Metadata metadata = new Metadata();
    String objectType = "";
    String logicalID = "";
    String version = "";
    String title = "";
    String associationType = "";
    List<TinyElementImpl> references = new ArrayList<TinyElementImpl>();
    List<Slot> slots = new ArrayList<Slot>();
    try {
      extractor.parse(product);
    } catch (Exception e) {
      throw new MetExtractionException("Parse failure: " + e.getMessage());
    }
    try {
      objectType = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
          Constants.PRODUCT_CLASS));
      logicalID = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
          Constants.LOGICAL_ID));
      version = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
          Constants.PRODUCT_VERSION));
      title = extractor.getValueFromDoc(Constants.coreXpathsMap.get(
          Constants.TITLE));
      associationType = extractor.getValueFromDoc(ASSOCIATION_TYPE_XPATH);
      references = extractor.getNodesFromDoc(Constants.coreXpathsMap.get(
          Constants.REFERENCES));
    } catch (Exception x) {
      //TODO: getMessage() doesn't always return a message
      throw new MetExtractionException(x.getMessage());
    }
    if (!"".equals(logicalID)) {
      metadata.addMetadata(Constants.LOGICAL_ID, logicalID);
    }
    if (!"".equals(version)) {
      metadata.addMetadata(Constants.PRODUCT_VERSION, version);
    }
    if (!"".equals(title)) {
      String trimmedTitle = title.replaceAll("\\s+", " ").trim();
      metadata.addMetadata(Constants.TITLE, trimmedTitle);
    }
    if (!"".equals(objectType)) {
       metadata.addMetadata(Constants.OBJECT_TYPE, objectType);
    }
    if ("".equals(associationType)) {
      throw new MetExtractionException("Could not find the inventory "
          + "reference_type element tag with the following xpath expression: "
          + ASSOCIATION_TYPE_XPATH);
    } else {
      // Map the reference type
      if (config.containsReferenceTypeMap()) {
        String refTypeMap = config.getReferenceTypeMap(associationType);
        if (refTypeMap != null) {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Mapping reference type '" + associationType + "' to '"
              + refTypeMap + "'.", product.toString()));
          associationType = refTypeMap;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "No mapping found for reference type '" + associationType
              + "'.", product.toString()));
        }
      }
    }
    if (references.size() == 0) {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "No associations found.",
          product));
    }
    if ((!"".equals(objectType)) && (config.hasObjectType(objectType))) {
      slots.addAll(extractMetadata(config.getMetXPaths(objectType)));
    }
    List<ReferenceEntry> refEntries = new ArrayList<ReferenceEntry>();
    try {
      refEntries.addAll(getReferences(references, product));
    } catch (Exception e) {
      throw new MetExtractionException(e.getMessage());
    }
    try {
      InventoryTableReader reader = new InventoryTableReader(product);
      for (InventoryEntry entry = new InventoryEntry(); entry != null;) {
        if (!entry.isEmpty()) {
          ReferenceEntry re = new ReferenceEntry();
          re.setLineNumber(reader.getLineNumber());
          re.setFile(reader.getDataFile());
          String identifier = entry.getIdentifier();
          if (!identifier.equals("")) {
            //Check for a LID or LIDVID
            if (identifier.indexOf("::") != -1) {
              re.setLogicalID(identifier.split("::")[0]);
              re.setVersion(identifier.split("::")[1]);
            } else {
              re.setLogicalID(identifier);
            }
          }
          re.setType(associationType);
          refEntries.add(re);
          if (!"P".equalsIgnoreCase(entry.getMemberStatus())) {
            Constants.nonPrimaryMembers.add(new LidVid(re.getLogicalID(),
                re.getVersion()));
          }
        }
        entry = reader.getNext();
      }
      if (refEntries.size() == 0) {
        log.log(new ToolsLogRecord(ToolsLevel.INFO,
            "No associations found.", reader.getDataFile()));
      } else {
        HashMap<String, List<String>> refMap =
            new HashMap<String, List<String>>();
        // Search for LID-based associations and register them as slots
        List<ReferenceEntry> lidVidEntries = new ArrayList<ReferenceEntry>();
        for (ReferenceEntry entry : refEntries) {
          String value = "";
          if (!entry.hasVersion()) {
            log.log(new ToolsLogRecord(ToolsLevel.INFO, "Setting "
                + "LID-based association, \'" + entry.getLogicalID()
                + "\', under slot name \'" + entry.getType()
                + "\'.", product));
            value = entry.getLogicalID();
          } else {
            String lidvid = entry.getLogicalID() + "::" + entry.getVersion();
            log.log(new ToolsLogRecord(ToolsLevel.INFO, "Setting "
                + "LIDVID-based association, \'" + lidvid
                + "\', under slot name \'" + entry.getType()
                + "\'.", product));
            value = lidvid;
          }
          if (!value.isEmpty()) {
            List<String> values = refMap.get(entry.getType());
            if (values == null) {
              values = new ArrayList<String>();
              refMap.put(entry.getType(), values);
              values.add(value);
            } else {
              values.add(value);
            }
          }
        }
        if (!refMap.isEmpty()) {
          for (Map.Entry<String, List<String>> entry : refMap.entrySet()) {
            slots.add(new Slot(entry.getKey(), entry.getValue()));
          }
        }
        
        if (!lidVidEntries.isEmpty()) {
        	Metadata submeta = new Metadata();
        	for (ReferenceEntry lidVidEntry : lidVidEntries) {
        		submeta.addMetadata(Constants.LOGICAL_ID, lidVidEntry.getLogicalID());
        	}
          metadata.addMetadata(Constants.REFERENCES, submeta);
        }
      }
    } catch (InventoryReaderException ie) {
      String message = "";
      if (ie.getLineNumber() != -1) {
        message += "line " + ie.getLineNumber() + ": ";
      }
      message += ie.getMessage();
      throw new MetExtractionException(message);
    }
    if (!slots.isEmpty()) {
        if (!slots.isEmpty()) {
        	Metadata submeta = new Metadata();
        	for (Slot slot : slots) {
        		submeta.addMetadata(slot.getName(), slot.getValues());
        	}
          metadata.addMetadata(Constants.SLOT_METADATA, submeta);
        }
    }
    return metadata;
  }
}
