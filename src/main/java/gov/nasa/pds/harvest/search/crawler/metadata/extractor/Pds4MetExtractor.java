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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.exception.ExceptionUtils;

import net.sf.saxon.tree.tiny.TinyElementImpl;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.XPath;
import gov.nasa.pds.harvest.search.util.LidVid;
import gov.nasa.pds.harvest.search.util.XMLExtractor;
import gov.nasa.pds.registry.model.Slot;

/**
 * Class to extract metadata from a PDS4 XML file.
 *
 * @author mcayanan
 *
 */
public class Pds4MetExtractor implements MetExtractor {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      Pds4MetExtractor.class.getName());

  /** A metadata extraction configuration. */
  protected Pds4MetExtractorConfig config;

  /** An XMLExtractor to get the metadata. */
  protected XMLExtractor extractor;

  /**
   * Default constructor.
   *
   * @param config The configuration that contains what metadata
   * and what object types to extract.
   */
  public Pds4MetExtractor(Pds4MetExtractorConfig config) {
    this.config = config;
    extractor = new XMLExtractor();
  }

  /**
   * Extract the metadata
   *
   * @param product A PDS4 xml file
   * @return a class representation of the extracted metadata
   *
   * @throws MetExtractionException If an error occured while performing
   * metadata extraction.
   *
   */
  public Metadata extractMetadata(File product)
  throws MetExtractionException {
    Metadata metadata = new Metadata();
    String objectType = "";
    String logicalID = "";
    String version = "";
    String title = "";
    List<TinyElementImpl> references = new ArrayList<TinyElementImpl>();
    List<TinyElementImpl> dataClasses = new ArrayList<TinyElementImpl>();
    List<Slot> slots = new ArrayList<Slot>();
    try {
      extractor.parse(product);
    } catch (Exception e) {
      throw new MetExtractionException("Parse failure: "
          + e.getMessage());
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
      references = extractor.getNodesFromDoc(Constants.coreXpathsMap.get(
          Constants.REFERENCES));
      dataClasses = extractor.getNodesFromDoc(Constants.coreXpathsMap.get(
          Constants.DATA_CLASS));
    } catch (Exception x) {
      throw new MetExtractionException(ExceptionUtils.getRootCauseMessage(x));
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
    if (references.size() == 0) {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "No associations found.",
          product));
    }
    if ((!"".equals(objectType)) && (config.hasObjectType(objectType))) {
      slots.addAll(extractMetadata(config.getMetXPaths(objectType)));
    }
    if (dataClasses.size() != 0) {
      List<String> values = new ArrayList<String>();
      for (TinyElementImpl dataClass : dataClasses) {
         values.add(dataClass.getDisplayName());
      }
      slots.add(new Slot(Constants.DATA_CLASS, values));
    }
    try {
      HashMap<String, List<String>> refMap =
          new HashMap<String, List<String>>();
      // Register LID-based and LIDVID-based associations as slots
      for (ReferenceEntry entry : getReferences(references, product)) {
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
        List<String> values = refMap.get(entry.getType());
        if (values == null) {
          values = new ArrayList<String>();
          refMap.put(entry.getType(), values);
          values.add(value);
        } else {
          values.add(value);
        }
      }
      if (!refMap.isEmpty()) {
        for (Map.Entry<String, List<String>> entry : refMap.entrySet()) {
          slots.add(new Slot(entry.getKey(), entry.getValue()));
        }
      }
    } catch (Exception e) {
      throw new MetExtractionException(ExceptionUtils.getRootCauseMessage(e));
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

  /**
   * Extracts metadata
   *
   * @param xPaths A list of xpath expressions.
   *
   * @return A list of Slots that contain the extracted metadata.
   *
   * @throws MetExtractionException If a bad xPath expression was
   *  encountered.
   */
  protected List<Slot> extractMetadata(List<XPath> xPaths)
  throws MetExtractionException {
    List<Slot> slots = new ArrayList<Slot>();
    for (XPath xpath : xPaths) {
      try {
        TinyElementImpl node = extractor.getNodeFromDoc(
            xpath.getValue());
        String name = "";
        if (xpath.getSlotName() != null) {
          name = xpath.getSlotName();
        } else {
          if (node != null) {
            name = node.getDisplayName();
          }
        }
        List<String> values = extractor.getValuesFromDoc(xpath.getValue());
        if (values != null && (!values.isEmpty())) {
          Slot slot = new Slot(name, values);
          String unit = node.getAttributeValue("", Constants.UNIT);
          if (unit != null) {
            slot.setSlotType(unit);
          }
          slots.add(slot);
        }
      } catch (Exception xe) {
        throw new MetExtractionException("Bad XPath Expression: "
            + xpath.getValue());
      }
    }
    return slots;
  }

  /**
   * Extracts the metadata found in an association entry.
   *
   * @param references A list of association entries.
   * @param product The product.
   *
   * @return A list of ReferenceEntry objects, which holds the association
   * metadata.
   *
   * @throws XPathExpressionException If there was an invalid XPath
   * expression.
   * @throws MetExtractionException
   */
  protected List<ReferenceEntry> getReferences(
      List<TinyElementImpl> references, File product)
  throws XPathExpressionException, MetExtractionException {
    List<ReferenceEntry> refEntries = new ArrayList<ReferenceEntry>();
    String REFERENCE_TYPE = "reference_type";
    String name = "";
    String value = "";
    for (TinyElementImpl reference : references) {
      List<TinyElementImpl> children = extractor.getNodesFromItem("*",
          reference);
      List<LidVid> lidvids = new ArrayList<LidVid>();
      String refTypeMap = "";
      for (TinyElementImpl child : children) {
        name = child.getLocalPart();
        value = child.getStringValue();
        if (name.equals("lidvid_reference")) {
          try {
            String[] tokens = value.split("::");
            lidvids.add(new LidVid(tokens[0], tokens[1]));
          } catch (ArrayIndexOutOfBoundsException ae) {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Expected "
                + "a LID-VID reference, but found this: " + value,
                product.toString(),
            child.getLineNumber()));
            break;
          }
        } else if (name.equals("lid_reference")) {
          lidvids.add(new LidVid(value));
        } else if (name.equals(REFERENCE_TYPE)) {
          if (config.containsReferenceTypeMap()) {
            refTypeMap = config.getReferenceTypeMap(value);
            if (refTypeMap != null) {
              log.log(new ToolsLogRecord(ToolsLevel.INFO,
                  "Mapping reference type '" + value + "' to '"
                  + refTypeMap + "'.", product.toString(),
                  child.getLineNumber()));
            } else {
              log.log(new ToolsLogRecord(ToolsLevel.WARNING,
                  "No mapping found for reference type '" + value
                  + "'.", product.toString(), child.getLineNumber()));
              refTypeMap = value;
            }
          } else {
            refTypeMap = value;
          }
        }
      }
      if (lidvids.isEmpty()) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, 
            "Missing one or more 'lidvid_reference' or 'lid_reference' elements.",
            product.toString(), reference.getLineNumber()));
      } else if (refTypeMap.isEmpty()) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Could not find \'"
            + REFERENCE_TYPE + "\' element.", product.toString(),
            reference.getLineNumber()));        
      } else {
        // This allows us to support the one-to-many relationship
        // use case (i.e. Source_Product_Internal area can contain
        // multiple lidvids that are associated to a single reference type)
        for (LidVid lidvid : lidvids) {
          ReferenceEntry re = new ReferenceEntry();
          if (lidvid.hasVersion()) {
            re.setLogicalID(lidvid.getLid());
            re.setVersion(lidvid.getVersion());
          } else {
            re.setLogicalID(lidvid.getLid());
          }
          re.setFile(product);
          re.setType(refTypeMap);
          re.setLineNumber(reference.getLineNumber());
          refEntries.add(re);
        }
      }
    }
    return refEntries;
  }

  /**
   * Extract the metadata.
   *
   * @param product A PDS4 xml file.
   * @return a class representation of the extracted metadata.
   *
   */
  public Metadata extractMetadata(String product)
  throws MetExtractionException {
    return extractMetadata(new File(product));
  }

  /**
   * Extract the metadata.
   *
   * @param product A PDS4 xml file.
   * @return a class representation of the extracted metadata.
   *
   */
  public Metadata extractMetadata(URL product)
  throws MetExtractionException {
    return extractMetadata(product.toExternalForm());
  }

  /**
   * No need to be implemented.
   *
   */
  public Metadata extractMetadata(File product, File configFile)
  throws MetExtractionException {
    // No need to implement at this point
    return null;
  }

  /**
   * No need to be implemented.
   *
   */
  public Metadata extractMetadata(File product, String configFile)
  throws MetExtractionException {
    // No need to implement at this point
    return null;
  }

  /**
   * No need to be implemented.
   *
   */
  public Metadata extractMetadata(File product, MetExtractorConfig config)
  throws MetExtractionException {
    setConfigFile(config);
    return extractMetadata(product);
  }

  /**
   * No need to be implemented.
   *
   */
  public Metadata extractMetadata(URL product, MetExtractorConfig config)
  throws MetExtractionException {
    setConfigFile(config);
    return extractMetadata(product);
  }

  /**
   * No need to be implemented.
   *
   */
  public void setConfigFile(File configFile)
  throws MetExtractionException {
    // No need to implement at this point
  }

  /**
   * No need to be implemented.
   *
   */
  public void setConfigFile(String configFile)
  throws MetExtractionException {
    // No need to implement at this point
  }

  public void setConfigFile(MetExtractorConfig config) {
    this.config = (Pds4MetExtractorConfig) config;
  }
}
