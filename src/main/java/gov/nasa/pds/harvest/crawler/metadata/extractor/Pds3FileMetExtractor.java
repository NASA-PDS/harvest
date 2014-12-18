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
package gov.nasa.pds.harvest.crawler.metadata.extractor;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractorConfig;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.file.FileSize;
import gov.nasa.pds.harvest.file.MD5Checksum;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;
import gov.nasa.pds.registry.model.Slot;

/**
 * A class to extract metadata information from PDS3 files.
 *
 * @author mcayanan
 *
 */
public class Pds3FileMetExtractor implements MetExtractor {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      Pds3FileMetExtractor.class.getName());

  /** Holds the metadata extractor configuration. */
  private Pds3MetExtractorConfig config;

  /** Flag to enable generation of checksums on the fly. */
  private boolean generateChecksums;

  /** Represents the checksum manifest file. */
  private Map<File, String> checksumManifest;

  public Pds3FileMetExtractor(Pds3MetExtractorConfig config) {
    this.config = config;
  }

  @Override
  public Metadata extractMetadata(File product)
      throws MetExtractionException {
    Metadata metadata = new Metadata();
    metadata.addMetadata(Constants.OBJECT_TYPE, Constants.FILE_OBJECT_PRODUCT_TYPE);
    String lid = config.getLidContents().getPrefix();
    if (config.getLidContents().isAppendDir()) {
      String parent = product.getParent();
      String offset = config.getLidContents().getOffset();
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
    if (config.getLidContents().isAppendFilename()) {
      lid += ":" + FilenameUtils.getBaseName(product.toString());
    }
    lid += ":" + product.getName();
    lid = lid.toLowerCase();
    metadata.addMetadata(Constants.LOGICAL_ID, lid);
    metadata.addMetadata(Constants.PRODUCT_VERSION, "1.0");
    List<String> fileTypes = new ArrayList<String>();
    fileTypes.add("Extra");
    List<Slot> slots = new ArrayList<Slot>();
    if (!config.getStaticMetadata().isEmpty()) {
      for (gov.nasa.pds.harvest.policy.Slot slot : config.getStaticMetadata()) {
        if (slot.getName().equals(Constants.PRODUCT_VERSION)) {
          metadata.replaceMetadata(Constants.PRODUCT_VERSION, slot.getValue());
        } else if (slot.getName().equals(Constants.FILE_TYPE)) {
          fileTypes = slot.getValue();
        } else {
          slots.add(new Slot(slot.getName(), slot.getValue()));
        }
      }
    }
    try {
      FileObject fileObject = createFileObject(product);
      metadata.addMetadata(Constants.TITLE, FilenameUtils.getBaseName(
          fileObject.getName()));
      slots.add(new Slot(Constants.FILE_NAME,
          Arrays.asList(new String[]{fileObject.getName()})));

      slots.add(new Slot(Constants.FILE_LOCATION,
          Arrays.asList(new String[]{fileObject.getLocation()})));

      FileSize fs = fileObject.getSize();
      Slot fsSlot = new Slot(Constants.FILE_SIZE, Arrays.asList(
          new String[]{new Long(fs.getSize()).toString()}));
      if (fs.hasUnits()) {
        fsSlot.setSlotType(fs.getUnits());
      }
      slots.add(fsSlot);

      slots.add(new Slot(Constants.MIME_TYPE,
          Arrays.asList(new String[]{fileObject.getMimeType()})));

      if ( (fileObject.getChecksum()) != null
          && (!fileObject.getChecksum().isEmpty()) ) {
        slots.add(new Slot(Constants.MD5_CHECKSUM,
            Arrays.asList(new String[]{fileObject.getChecksum()})));
      }

      slots.add(new Slot(Constants.FILE_TYPE, fileTypes));

      slots.add(new Slot(Constants.CREATION_DATE_TIME,
          Arrays.asList(new String[]{fileObject.getCreationDateTime()})));
    } catch (Exception e) {
      throw new MetExtractionException(e.getMessage());
    }
    if (!slots.isEmpty()) {
      metadata.addMetadata(Constants.SLOT_METADATA, slots);
    }
    return metadata;
  }

  private FileObject createFileObject(File product) throws Exception {
    SimpleDateFormat format = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
    String lastModified = format.format(new Date(product.lastModified()));
    String checksum = handleChecksum(product, product, null);
    FileObject fileObject = new FileObject(product.getName(),
        product.getParent(), new FileSize(product.length(), Constants.BYTE),
        lastModified, checksum, "");
    return fileObject;
  }

  /**
   * Method to handle checksum processing.
   *
   * @param product The source (product label).
   * @param fileObject The associated file object.
   * @param checksumInLabel Supplied checksum in the label. Can pass in
   * an empty value.
   *
   * @return The resulting checksum. This will either be the generated value,
   * the value from the manifest file (if supplied), or the value from the
   * supplied value in the product label (if provided).
   *
   * @throws Exception If there was an error generating the checksum
   *  (if the flag was on)
   */
  private String handleChecksum(File product, File fileObject,
      String checksumInLabel)
  throws Exception {
    String result = "";
    if (generateChecksums) {
      String generatedChecksum = MD5Checksum.getMD5Checksum(
          fileObject.toString());
      if (!checksumManifest.isEmpty()) {
        if (checksumManifest.containsKey(fileObject)) {
          String suppliedChecksum = checksumManifest.get(fileObject);
          if (!suppliedChecksum.equals(generatedChecksum)) {
            log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Generated checksum '" + generatedChecksum
              + "' does not match supplied checksum '"
              + suppliedChecksum + "' in the manifest for file object '"
              + fileObject.toString() + "'.", product));
            ++HarvestStats.numGeneratedChecksumsDiffInManifest;
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Generated checksum '" + generatedChecksum
              + "' matches the supplied checksum '" + suppliedChecksum
              + "' in the manifest for file object '" + fileObject.toString()
              + "'.", product));
            ++HarvestStats.numGeneratedChecksumsSameInManifest;
          }
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "No checksum found in the manifest for file object '"
              + fileObject.toString() + "'.", product));
          ++HarvestStats.numGeneratedChecksumsNotCheckedInManifest;
        }
      }
      result = generatedChecksum;
    }
    return result;
  }

  /**
   * Set the flag for checksum generation.
   *
   * @param value 'true' to turn on, 'false' to turn off.
   */
  public void setGenerateChecksums(boolean value) {
    this.generateChecksums = value;
  }

  /**
   * Set the map to represent the checksum manifest file.
   *
   * @param manifest A mapping of file objects to checksums.
   */
  public void setChecksumManifest(Map<File, String> manifest) {
    this.checksumManifest = manifest;
  }

  @Override
  public Metadata extractMetadata(String product)
      throws MetExtractionException {
    return extractMetadata(new File(product));
  }

  @Override
  public Metadata extractMetadata(URL product) throws MetExtractionException {
    return extractMetadata(product.toExternalForm());
  }

  @Override
  public Metadata extractMetadata(File product, File configFile)
      throws MetExtractionException {
    // No need to implement at this point
    return null;
  }

  @Override
  public Metadata extractMetadata(File product, String configFile)
      throws MetExtractionException {
    // No need to implement at this point
    return null;
  }

  @Override
  public Metadata extractMetadata(File product, MetExtractorConfig config)
      throws MetExtractionException {
    setConfigFile(config);
    return extractMetadata(product);
  }

  @Override
  public Metadata extractMetadata(URL product, MetExtractorConfig config)
      throws MetExtractionException {
    setConfigFile(config);
    return extractMetadata(product);
  }

  @Override
  public void setConfigFile(File configFile) throws MetExtractionException {
    // No need to implement at this point

  }

  @Override
  public void setConfigFile(String configFile) throws MetExtractionException {
    // No need to implement at this point

  }

  @Override
  public void setConfigFile(MetExtractorConfig config) {
    this.config = (Pds3MetExtractorConfig) config;
  }

}
