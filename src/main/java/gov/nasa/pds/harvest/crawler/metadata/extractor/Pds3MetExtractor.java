// Copyright 2006-2011, by the California Institute of Technology.
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

package gov.nasa.pds.harvest.crawler.metadata.extractor;

import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractorConfig;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.file.MD5Checksum;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.policy.Association;
import gov.nasa.pds.harvest.util.PointerStatementFinder;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.containers.FileReference;
import gov.nasa.pds.tools.label.AttributeStatement;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.ManualPathResolver;
import gov.nasa.pds.tools.label.PointerStatement;
import gov.nasa.pds.tools.label.parser.DefaultLabelParser;
import gov.nasa.pds.tools.util.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
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
    // Get the values of all required PDS3 keywords
    Metadata pds3Met = new Metadata();
    for (String key : Constants.pds3ToPds4Map.keySet()) {
      AttributeStatement attribute = label.getAttribute(key);
      if (attribute != null) {
        pds3Met.addMetadata(key, attribute.getValue().toString());
      }
    }
    // Register the values using the PDS4 equivalent metadata key
    for (Entry<String, String> entry : Constants.pds3ToPds4Map.entrySet()) {
      if (pds3Met.containsKey(entry.getKey())) {
        metadata.addMetadata(entry.getValue(), pds3Met.getMetadata(
            entry.getKey()));
      }
    }
    metadata.addMetadata(Constants.OBJECT_TYPE, "Product_Proxy_PDS3");
    String dataSetId = "";
    String productId = "";
    try {
      dataSetId = label.getAttribute("DATA_SET_ID").getValue().toString();
    } catch (NullPointerException n) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING, "DATA_SET_ID not found.",
          product));
    }
    try {
      productId = label.getAttribute("PRODUCT_ID").getValue().toString();
    } catch (NullPointerException n) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING, "PRODUCT_ID not found. "
          + "Using file name to create the logical identifier.", product));
      productId = FilenameUtils.getBaseName(product.toString());
    }
    if (dataSetId.isEmpty() && productId.isEmpty()) {
      throw new MetExtractionException("Could not create a logical " +
          "identifier due to missing DATA_SET_ID and PRODUCT_ID from the label.");
    }
    //ATMOS example used INSTRUMENT_ID/INSTRUMENT_NAME in LID
    //Should we do the same?
    String lid = "";
    if (config.getLidPrefix() != null) {
      lid += config.getLidPrefix() + ":" + dataSetId + ":" + productId;
    } else {
      lid += dataSetId + ":" + productId;
    }
    //Product ID or Product Version values may have slash characters
    //Replace it with a dash character
    lid = lid.replaceAll("(/|\\\\)", "-");
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
    String title = "";
    if (pds3Met.containsKey("INSTRUMENT_HOST_NAME")) {
      title += pds3Met.getMetadata("INSTRUMENT_HOST_NAME") + " ";
    }
    if (pds3Met.containsKey("INSTRUMENT_NAME")) {
      title += pds3Met.getMetadata("INSTRUMENT_NAME") + " ";
    } else {
      if (pds3Met.containsKey("INSTRUMENT_ID")) {
        title += pds3Met.getMetadata("INSTRUMENT_ID") + " ";
      }
    }
    //This is a default title.
    if (title.trim().isEmpty()) {
      title = "PDS3 Data Product";
    }
    metadata.addMetadata(Constants.TITLE, title);

    List<ReferenceEntry> references = getReferences(config.getAssociations(),
        product);
    if (!references.isEmpty()) {
      // Register LID-based and LIDVID-based associations as slots
      for (ReferenceEntry entry : references) {
        if (!entry.hasVersion()) {
          metadata.addMetadata(entry.getType(),
              entry.getLogicalID());
          log.log(new ToolsLogRecord(ToolsLevel.INFO, "Setting "
              + "LID-based association, \'" + entry.getLogicalID()
              + "\', under slot name \'" + entry.getType()
              + "\'.", product));
        } else {
          String lidvid = entry.getLogicalID() + "::" + entry.getVersion();
          metadata.addMetadata(entry.getType(), lidvid);
          log.log(new ToolsLogRecord(ToolsLevel.INFO, "Setting "
              + "LIDVID-based association, \'" + lidvid
              + "\', under slot name \'" + entry.getType()
              + "\'.", product));
        }
      }
    } else {
      log.log(new ToolsLogRecord(ToolsLevel.INFO,
          "No associations found.", product));
    }
    List<FileObject> fileObjectEntries = new ArrayList<FileObject>();
    try {
      fileObjectEntries = getFileObjects(product, label);
    } catch (Exception e) {
      throw new MetExtractionException("Error occurred while getting file "
          + "objects: " + e.getMessage());
    }
    metadata.addMetadata(Constants.FILE_OBJECTS, fileObjectEntries);
    // Register additional metadata (if specified)
    if (!config.getAncillaryMetadata().isEmpty()) {
      for (String element : config.getAncillaryMetadata()) {
        try {
          metadata.addMetadata(element.toLowerCase(),
              label.getAttribute(element).getValue().toString());
        } catch (NullPointerException n) {
          // Ignore. Element was not found in the label.
        }
      }
    }
    return metadata;
  }

  private List<ReferenceEntry> getReferences(List<Association> associations,
      File product) {
    List<ReferenceEntry> references = new ArrayList<ReferenceEntry>();
    for(Association association : associations) {
      ReferenceEntry entry = new ReferenceEntry();
      String lidvid = association.getLidVidReference();
      // Check for a lid or lidvid reference
      if (lidvid != null) {
        try {
          entry.setLogicalID(lidvid.split("::")[0]);
          entry.setVersion(lidvid.split("::")[1]);
        } catch (ArrayIndexOutOfBoundsException ae) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Expected "
              + "a LID-VID reference, but found this: " + lidvid,
              product.toString()));
        }
      } else {
        entry.setLogicalID(association.getLidReference());
      }
      entry.setType(association.getReferenceType());
      references.add(entry);
    }
    return references;
  }

  private List<FileObject> getFileObjects(File product, Label label)
  throws URISyntaxException, MalformedURLException {
    SimpleDateFormat format = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
    List<FileObject> results = new ArrayList<FileObject>();
    // Create a file object of the label file
    String lastModified = format.format(new Date(product.lastModified()));
    try {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file object "
          + "metadata for " + product.getName(), product));
      FileObject fileObject = new FileObject(product.getName(),
          product.getParent(), product.length(),
          lastModified, MD5Checksum.getMD5Checksum(product.toString()));
      results.add(fileObject);
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
          + "occurred while calculating checksum for " + product.getName()
          + ": " + e.getMessage(), product.toString()));
    }
    // File references are found in pointer statements in a label.
    String basePath = product.getParent();
    List<PointerStatement> pointers = PointerStatementFinder.find(label);
    for (PointerStatement ps : pointers) {
      for (FileReference fileRef : ps.getFileRefs()) {
        File file = resolvePath(fileRef.getPath(), basePath,
            config.getIncludePaths());
        try {
          if (file != null) {
            if (!file.getName().equals(product.getName())) {
              log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file "
                + "object metadata for " + file.getName(), product));
              long size = file.length();
              String creationDateTime = format.format(new Date(
                file.lastModified()));
              String checksum = MD5Checksum.getMD5Checksum(file.toString());
              results.add(new FileObject(file.getName(), file.getParent(),
                  size, creationDateTime, checksum));
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "File object not "
                + "found: " + fileRef.getPath(), product.toString()));
          }
        } catch (Exception e) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error occurred "
              + "while calculating checksum for " + file.getName() + ": ",
              product));
        }
      }
    }
    return results;
  }

  private File resolvePath(String name, String basePath,
      List<String> includePaths) {
    File file = new File(basePath, name);
    if (file.exists()) {
      return file;
    } else {
      for (String includePath : includePaths) {
        file = new File(includePath, name);
        if (file.exists()) {
          return file;
        }
      }
    }
    return null;
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
