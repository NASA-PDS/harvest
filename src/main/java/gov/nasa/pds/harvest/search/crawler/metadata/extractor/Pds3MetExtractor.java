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

import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.ElementName;
import gov.nasa.pds.harvest.search.policy.LidContents;
import gov.nasa.pds.harvest.search.policy.TitleContents;
import gov.nasa.pds.harvest.search.util.StatementFinder;
import gov.nasa.pds.registry.model.Slot;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.label.AttributeStatement;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.ManualPathResolver;
import gov.nasa.pds.tools.label.Sequence;
import gov.nasa.pds.tools.label.Set;
import gov.nasa.pds.tools.label.Value;
import gov.nasa.pds.tools.label.parser.DefaultLabelParser;
import gov.nasa.pds.tools.util.MessageUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * A class to extract metadata from a PDS3 data product label.
 *
 * @author mcayanan
 *
 */
public class Pds3MetExtractor implements MetExtractor {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          Pds3MetExtractor.class.getName());

  /** Holds the metadata extractor configuration. */
  private Pds3MetExtractorConfig config;

  /** Label parser. */
  private DefaultLabelParser parser;

  /**
   * Constructor.
   *
   * @param config A configuration object for the metadata extractor.
   */
  public Pds3MetExtractor(Pds3MetExtractorConfig config) {
    this.config = config;
    ManualPathResolver resolver = new ManualPathResolver();
    parser = new DefaultLabelParser(false, true, resolver);
  }

  /**
   * Extract the metadata from the given file.
   *
   * @param product The PDS3 label file.
   *
   * @return A metadata object containing the extracted metadata.
   */
  public Metadata extractMetadata(File product)
  throws MetExtractionException {
    Metadata metadata = new Metadata();
    Label label = null;
    try {
      label = parser.parseLabel(product.toURI().toURL());
    } catch (LabelParserException lp) {
      throw new MetExtractionException(MessageUtils.getProblemMessage(lp));
    } catch (Exception e) {
      throw new MetExtractionException(e.getMessage());
    }
    metadata.addMetadata(Constants.OBJECT_TYPE, "Product_Proxy_PDS3");

    String lid = createLid(product, label, config.getLidContents());
    metadata.addMetadata(Constants.LOGICAL_ID, lid);
    //Get the value of PRODUCT_VERSION or default to 1.0
    try {
      String productVersion =
        label.getAttribute("PRODUCT_VERSION").getValue().toString();
      metadata.addMetadata(Constants.PRODUCT_VERSION, productVersion);
    } catch (NullPointerException n) {
      metadata.addMetadata(Constants.PRODUCT_VERSION, "1.0");
    }
    //Create a title
    String title = createTitle(product, label, config.getTitleContents());

    //This is a default title.
    if (title.trim().isEmpty()) {
      title = "PDS3 Data Product";
    }
    String trimmedTitle = title.replaceAll("\\s+", " ").trim();
    metadata.addMetadata(Constants.TITLE, trimmedTitle);

    // Capture the include paths for file object processing.
    metadata.addMetadata(Constants.INCLUDE_PATHS, config.getIncludePaths());

    List<Slot> slots = new ArrayList<Slot>();
    // Register any static metadata that is specified in the policy config
    if (!config.getStaticMetadata().isEmpty()) {
      for (gov.nasa.pds.harvest.search.policy.Slot slot : config.getStaticMetadata()) {
        slots.add(new Slot(slot.getName(), slot.getValue()));
      }
    }

    // Register additional metadata (if specified)
    if (!config.getAncillaryMetadata().isEmpty()) {
      for (ElementName element : config.getAncillaryMetadata()) {
        List<AttributeStatement> attributes = StatementFinder
        .getStatementsRecursively(label, element.getValue().trim());
        List<String> extractedValues = new ArrayList<String>();
        for (AttributeStatement attribute : attributes) {
          Value value = attribute.getValue();
          if (value instanceof Sequence || value instanceof Set) {
            List<String> multValues = new ArrayList<String>();
            Collection collection = (Collection) value;
            for (Object o : collection) {
              multValues.add(o.toString());
            }
            extractedValues.addAll(multValues);
          } else {
            extractedValues.add(value.toString());
          }
        }
        Slot slot = null;
        if (element.getSlotName() != null) {
          slot = new Slot(element.getSlotName(), extractedValues);
        } else {
          slot = new Slot(element.getValue().toLowerCase(), extractedValues);
        }
        if (element.getSlotType() != null) {
          slot.setSlotType(element.getSlotType());
        }
        slots.add(slot);
      }
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
   * Creates the logical identifier for the PDS3 product.
   *
   * @param product The PDS3 file being registered.
   * @param label The object representation of the PDS3 label.
   * @param lidContents The user-specified lid contents.
   * @return A logical identifier.
   *
   * @throws MetExtractionException
   */
  private String createLid(File product, Label label,
      LidContents lidContents) throws MetExtractionException {
    log.log(new ToolsLogRecord(ToolsLevel.INFO,
        "Creating logical identifier.", product));
    String lid ="";
    List<String> elementValues = new ArrayList<String>();
    for (ElementName name : lidContents.getElementName()) {
      try {
        Value value = label.getAttribute(name.getValue().trim())
        .getValue();
        String val = "";
        // Get only the first value if multiple values are specified for
        // an element.
        if (value instanceof Sequence || value instanceof Set) {
          Collection collection = (Collection) value;
          val = collection.iterator().next().toString();
        } else {
          val = value.toString();
        }
        elementValues.add(val);
      } catch (NullPointerException n) {
        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
            name.getValue() + " not found.", product));
      }
    }
    lid = lidContents.getPrefix();
    for (String elementValue : elementValues) {
      lid += ":" + elementValue;
    }
    if (lidContents.isAppendDir()) {
      String parent = product.getParent();
      String offset = lidContents.getOffset();
      if (offset != null) {
        boolean matchedOffset = false;
        if (parent.startsWith(offset)) {
          parent = parent.replace(offset, "")
            .trim();
          matchedOffset = true;
        }
        if ( (offset != null) && (!matchedOffset) ) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Cannot trim path of product '" + product
              + "' as it does not start with the supplied offset: "
              + offset, product));
        }
      }
      if (!parent.isEmpty()) {
        parent = parent.replaceAll("[/|\\\\]", ":");
        if (parent.startsWith(":")) {
          lid += parent.toLowerCase();
        } else {
          lid += ":" + parent.toLowerCase();
        }
      }
    }
    if (lidContents.isAppendFilename()) {
      lid += ":" + FilenameUtils.getBaseName(product.toString());
    }
    lid = lid.toLowerCase();
    log.log(new ToolsLogRecord(ToolsLevel.INFO,
        "Created the following logical identifier: " + lid, product));
    //Product ID or Product Version values may have slash characters
    //Replace it with a dash character
    String conformingLid = lid.replaceAll(Constants.URN_ILLEGAL_CHARACTERS, "-");
    //Replace whitespaces with an underscore
    conformingLid = conformingLid.replaceAll("\\s+", "_");
    if (!conformingLid.equals(lid)) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING, "logical identifier "
          + "contained URN reserved and/or excluded characters. "
          + "Converting logical identifier to the following: "
          + conformingLid, product));
    }
    return conformingLid;
  }

  /**
   * Creates the title of the PDS3 Proxy Product.
   *
   * @param product The product.
   * @param label The product label.
   * @param titleContents The title contents.
   *
   * @return The resulting value.
   */
  private String createTitle(File product, Label label,
      TitleContents titleContents) {
    List<String> elementValues = new ArrayList<String>();
    for (ElementName name : titleContents.getElementName()) {
      try {
        Value value = label.getAttribute(name.getValue().trim())
        .getValue();
        String val = "";
        // Get only the first value if multiple values are specified for
        // an element.
        if (value instanceof Sequence || value instanceof Set) {
          Collection collection = (Collection) value;
          val = collection.iterator().next().toString();
        } else {
          val = value.toString();
        }
        elementValues.add(val);
      } catch (NullPointerException n) {
        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
            name.getValue() + " not found.", product));
      }
    }
    String title = "";
    for (String elementValue : elementValues) {
        title += " " + elementValue;
    }
    if (titleContents.isAppendFilename()) {
      title += " " + FilenameUtils.getBaseName(product.toString());
    }
    log.log(new ToolsLogRecord(ToolsLevel.INFO,
        "Created title: " + title.trim(), product));
    return title.trim();
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
      this.config = (Pds3MetExtractorConfig) config;
  }
}
