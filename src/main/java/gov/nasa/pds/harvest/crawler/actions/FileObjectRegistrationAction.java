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

package gov.nasa.pds.harvest.crawler.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.tiny.TinyElementImpl;

import org.apache.commons.lang.exception.ExceptionUtils;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.file.FileSize;
import gov.nasa.pds.harvest.file.MD5Checksum;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.policy.FileTypeMap;
import gov.nasa.pds.harvest.policy.FileTypes;
import gov.nasa.pds.harvest.stats.HarvestStats;
import gov.nasa.pds.harvest.util.PointerStatementFinder;
import gov.nasa.pds.harvest.util.XMLExtractor;
import gov.nasa.pds.registry.exception.RegistryClientException;
import gov.nasa.pds.tools.LabelParserException;
import gov.nasa.pds.tools.containers.FileReference;
import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.ManualPathResolver;
import gov.nasa.pds.tools.label.PointerStatement;
import gov.nasa.pds.tools.label.parser.DefaultLabelParser;
import gov.nasa.pds.tools.util.MessageUtils;


/**
 * Class to register file objects.
 *
 * @author mcayanan
 *
 */
public class FileObjectRegistrationAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          FileObjectRegistrationAction.class.getName());

  /** Crawler action ID. */
  private final String ID = "FileObjectRegistrationAction";

  /** Crawler action description. */
  private final String DESCRIPTION = "Registers file objects associated with "
    + "a product.";

  /** Registry URL. */
  private String registryUrl;

  /** The registry client. */
  private RegistryIngester registryIngester;

  /** A list of actions to perform before file object registration. */
  private List<CrawlerAction> actions;

  /** Flag to enable generation of checksums on the fly. */
  private boolean generateChecksums;

  /** Represents the checksum manifest file. */
  private Map<File, String> checksumManifest;

  /** Represents a mapping of File_Area_* element names to a
   * file type.
   */
  private FileTypes fileTypes;

  /**
   * Constructor.
   *
   * @param registryUrl url of the registry.
   * @param ingester The RegistryIngester.
   * @throws RegistryClientException
   */
  public FileObjectRegistrationAction(String registryUrl,
      RegistryIngester ingester) {
      super();
      String []phases = {CrawlerActionPhases.POST_INGEST_SUCCESS};
      setPhases(Arrays.asList(phases));
      setId(ID);
      setDescription(DESCRIPTION);
      this.registryUrl = registryUrl;
      this.registryIngester = ingester;
      this.actions = new ArrayList<CrawlerAction>();
      this.generateChecksums = false;
      this.checksumManifest = new HashMap<File, String>();
      this.fileTypes = new FileTypes();
  }

  /**
   * Perform the action to register the file products.
   *
   * @param product The file.
   * @param metadata The metadata associated with the file.
   *
   * @return Always returns true.
   *
   * @throws CrawlerActionException If an error occurred while processing
   * the file objects.
   */
  @Override
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    List<FileObject> fileObjectEntries = new ArrayList<FileObject>();
    try {
      if (product.toString().toUpperCase().endsWith(".XML")) {
        fileObjectEntries = getPds4FileObjects(product);
      } else {
        fileObjectEntries = getPds3FileObjects(product,
            metadata.getAllMetadata(Constants.INCLUDE_PATHS));
      }
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error while processing "
          + "file objects: " + ExceptionUtils.getRootCauseMessage(e),
          product));
      throw new CrawlerActionException(e.getMessage());
    }
    for (FileObject fileObject : fileObjectEntries) {
      String lid = metadata.getMetadata(Constants.LOGICAL_ID) + ":"
      + fileObject.getName();
      String vid = metadata.getMetadata(Constants.PRODUCT_VERSION);
      String lidvid = lid + "::" + vid;
      try {
        // Perform a set of actions before ingesting the file object
        for (CrawlerAction action : actions) {
          if (action instanceof StorageIngestAction) {
            StorageIngestAction siAction =
              (StorageIngestAction) action;
            String productId = siAction.performAction(product, fileObject,
                metadata);
            fileObject.setStorageServiceProductId(productId);
          } else if (action instanceof CreateAccessUrlsAction) {
            CreateAccessUrlsAction cauAction = (CreateAccessUrlsAction) action;
            List<String> urls = cauAction.performAction(product, fileObject);
            fileObject.setAccessUrls(urls);
          }
        }
        String guid = registryIngester.ingest(new URL(registryUrl), product,
            fileObject, metadata);
        log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
            "Successfully registered product: " + lidvid, product));
        log.log(new ToolsLogRecord(ToolsLevel.INFO,
            "Product has the following GUID: " + guid, product));
        ++HarvestStats.numAncillaryProductsRegistered;
        HarvestStats.addProductType(Constants.FILE_OBJECT_PRODUCT_TYPE,
            new File(fileObject.getLocation(), fileObject.getName()));
        // Create a reference entry of the file association and add that
          // back to the list of reference entries to be processed later.
        ReferenceEntry refEntry = new ReferenceEntry();
        refEntry.setLogicalID(lid);
        refEntry.setVersion(metadata.getMetadata(Constants.PRODUCT_VERSION));
        refEntry.setGuid(guid);
        refEntry.setType("file_ref");
        if (metadata.containsKey(Constants.REFERENCES)) {
          List<ReferenceEntry> refEntries = metadata.getAllMetadata(
              Constants.REFERENCES);
          refEntries.add(refEntry);
          metadata.replaceMetadata(Constants.REFERENCES, refEntries);
        } else {
          List<ReferenceEntry> refEntries = new ArrayList<ReferenceEntry>();
          refEntries.add(refEntry);
          metadata.addMetadata(Constants.REFERENCES, refEntries);
        }
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            ExceptionUtils.getRootCauseMessage(e),
            product));
        ++HarvestStats.numAncillaryProductsNotRegistered;
        throw new CrawlerActionException(e.getMessage());
      }
    }
    return true;
  }

  /**
   * Sets a list of crawler actions to perform before file object
   * registration.
   *
   * @param actions A list of crawler actions.
   */
  public void setActions(List<CrawlerAction> actions) {
    this.actions = actions;
  }

  private List<FileObject> getPds4FileObjects(File product)
  throws Exception {
    SimpleDateFormat format = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
    List<FileObject> results = new ArrayList<FileObject>();
    // Create a file object of the label file
    String lastModified = format.format(new Date(product.lastModified()));
    try {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file information "
          + "for " + product.getName(), product));
      String checksum = handleChecksum(product, product);
      FileObject fileObject = new FileObject(product.getName(),
          product.getParent(), 
          new FileSize(product.length(), Constants.BYTE),
          lastModified, checksum, "Label");
      results.add(fileObject);
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
          + "occurred while generating checksum for " + product.getName()
          + ": " + e.getMessage(), product.toString()));
      ++HarvestStats.numAncillaryProductsNotRegistered;
    }
    XMLExtractor extractor = new XMLExtractor();
    try {
      extractor.parse(product);
    } catch (Exception e) {
      throw new Exception("Parse failure: " + e.getMessage());
    }
    // Search for "xml:base" attributes within the merged XML. This will
    // tell us if there are any xincludes.
    List<String> xincludes = extractor.getAttributeValuesFromDoc("//@xml:base");
    for (String xinclude : xincludes) {
      File xincludeFile = new File(product.getParent(), xinclude).getCanonicalFile();
      log.log(new ToolsLogRecord(ToolsLevel.INFO,
          "Capturing file information for " + xincludeFile.getName(),
          product));
      try {
        if (xincludeFile.exists()) {
          String lastMod = format.format(new Date(xincludeFile.lastModified()));
          String checksum = handleChecksum(xincludeFile, product);
          FileObject fileObject = new FileObject(xincludeFile.getName(),
            xincludeFile.getParent(), 
            new FileSize(xincludeFile.length(), Constants.BYTE), 
            lastMod, checksum, "Label Fragment");
          results.add(fileObject);
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING, "File object does "
              + "not exist: " + xincludeFile, product));
          ++HarvestStats.numAncillaryProductsNotRegistered;
        }
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
            + "occurred while generating checksum for " + xincludeFile.getName()
            + ": " + e.getMessage(), product.toString()));
        ++HarvestStats.numAncillaryProductsNotRegistered;
      }
    }
    // Search for File_Area_*/File tags within the product label
    List<TinyElementImpl> fileObjects = extractor.getNodesFromDoc(
        Constants.coreXpathsMap.get(Constants.FILE_OBJECTS));
    for (TinyElementImpl file : fileObjects) {
      String fileType = "";
      NodeInfo parent = file.getParent();
      String fileLocation = product.getParent();
      String name = "";
      long size = -1;
      String checksum = "";
      String creationDateTime = "";
      String unit = "";
      List<TinyElementImpl> children = extractor.getNodesFromItem("*", file);
      for (TinyElementImpl child : children) {
        if ("file_name".equals(child.getLocalPart())) {
          name = child.getStringValue();
        } else if ("file_size".equals(child.getLocalPart())) {
          size = Long.parseLong(child.getStringValue());
          unit = child.getAttributeValue("", "unit");
          if (unit == null) {
            unit = "";
          }
        } else if ("md5_checksum".equals(child.getLocalPart())) {
          checksum = child.getStringValue();
        } else if ("creation_date_time".equals(child.getLocalPart())) {
          creationDateTime = child.getStringValue();
        } else if ("directory_path_name".equals(child.getLocalPart())) {
          //Append the directory_path_name value to the file location
          fileLocation = new File(fileLocation, child.getStringValue())
          .toString();
        }
      }
      try {
        if (name.isEmpty()) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Missing "
              + "'file_name' tag within the 'File' area",
              product.toString(), file.getLineNumber()));
          throw new Exception("Missing file_name tag");
        }
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file "
            + "object metadata for " + name, product));
        File f = new File(fileLocation, name);
        if (!f.exists()) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING, "File object does "
              + "not exist: " + f, product));
          throw new Exception("File does not exist");
        } else {
          if (size == -1) {
            size = f.length();
            unit = Constants.BYTE;
          }
          if (creationDateTime.isEmpty()) {
            creationDateTime = format.format(new Date(f.lastModified()));
          }
          try {
            if (!checksum.isEmpty()) {
              checksum = handleChecksum(product, f, checksum);
            } else {
              checksum = handleChecksum(product, f);
            }
          } catch (Exception e) {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
                + "occurred while calculating checksum for '" + name + "': "
                + e.getMessage(), product.toString()));
            throw new Exception("Missing checksum");
          }
          if (containsFileTypes()) {
            if (getFileType(parent.getLocalPart()) != null) {
              fileType = getFileType(parent.getLocalPart());
              log.log(new ToolsLogRecord(ToolsLevel.INFO,
                  "Setting file type for the file object '" + f.getName()
                  + "' to '" + fileType + "'", product.toString(),
                  file.getLineNumber()));
            } else {
              log.log(new ToolsLogRecord(ToolsLevel.WARNING,
                "No file type mapping provided for '"
                  + parent.getLocalPart() + "'",
                product.toString(), file.getLineNumber()));
            }
          }
          results.add(new FileObject(f.getName(), f.getParent(), 
              new FileSize(size, unit), creationDateTime, checksum, 
              fileType));
        }
      } catch (Exception e) {
        ++HarvestStats.numAncillaryProductsNotRegistered;
        //Ignore
      }
    }
    return results;
  }

  private List<FileObject> getPds3FileObjects(File product,
      List<String> includePaths)
  throws URISyntaxException, MalformedURLException, MetExtractionException {
    SimpleDateFormat format = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
    List<FileObject> results = new ArrayList<FileObject>();
    // Create a file object of the label file
    String lastModified = format.format(new Date(product.lastModified()));
    try {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file object "
          + "metadata for " + product.getName(), product));
      String checksum = handleChecksum(product, product);
      FileObject fileObject = new FileObject(product.getName(),
          product.getParent(), new FileSize(product.length(), Constants.BYTE),
          lastModified, checksum, "Label");
      results.add(fileObject);
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
          + "occurred while calculating checksum for " + product.getName()
          + ": " + e.getMessage(), product.toString()));
      ++HarvestStats.numAncillaryProductsNotRegistered;
    }
    Label label = null;
    try {
      DefaultLabelParser parser = new DefaultLabelParser(false, true,
          new ManualPathResolver());
      label = parser.parseLabel(product.toURI().toURL());
    } catch (LabelParserException lp) {
      throw new MetExtractionException(MessageUtils.getProblemMessage(lp));
    } catch (Exception e) {
      throw new MetExtractionException(e.getMessage());
    }
    // File references are found in pointer statements in a label.
    // TODO: What file types do we give for PDS3 registered products?
    String basePath = product.getParent();
    List<PointerStatement> pointers = PointerStatementFinder.find(label);
    for (PointerStatement ps : pointers) {
      for (FileReference fileRef : ps.getFileRefs()) {
        File file = resolvePath(fileRef.getPath(), basePath, includePaths);
        try {
          if (file != null) {
            if (!file.getName().equals(product.getName())) {
              log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file "
                + "object metadata for " + file.getName(), product));
              long size = file.length();
              String creationDateTime = format.format(new Date(
                file.lastModified()));
              String checksum = handleChecksum(product, file);
              results.add(new FileObject(file.getName(), file.getParent(),
                  new FileSize(size, Constants.BYTE), 
                  creationDateTime, checksum, "Observation"));
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "File object not "
                + "found: " + fileRef.getPath(), product.toString()));
            ++HarvestStats.numAncillaryProductsNotRegistered;
          }
        } catch (Exception e) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error occurred "
              + "while calculating checksum for " + file.getName() + ": ",
              product));
          ++HarvestStats.numAncillaryProductsNotRegistered;
        }
      }
    }
    return results;
  }

  private String handleChecksum(File product, File fileObject)
  throws Exception {
    return handleChecksum(product, fileObject, "");
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
      if (!checksumInLabel.isEmpty()) {
        if (!generatedChecksum.equals(checksumInLabel)) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Generated checksum '" + generatedChecksum
              + "' does not match supplied checksum '"
              + checksumInLabel + "' in the product label for file object '"
              + fileObject.toString() + "'.", product));
            ++HarvestStats.numGeneratedChecksumsDiffInLabel;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Generated checksum '" + generatedChecksum
              + "' matches the supplied checksum '" + checksumInLabel
              + "' in the produt label for file object '"
              + fileObject.toString() + "'.", product));
            ++HarvestStats.numGeneratedChecksumsSameInLabel;
        }
      } else {
        log.log(new ToolsLogRecord(ToolsLevel.INFO,
            "No checksum to compare against in the product label "
            + "for file object '" + fileObject.toString() + "'.", product));
        ++HarvestStats.numGeneratedChecksumsNotCheckedInLabel;
      }
      result = generatedChecksum;
    } else {
      // Checksums will not be generated
      if (!checksumManifest.isEmpty()) {
        if (checksumManifest.containsKey(fileObject)) {
          String suppliedChecksum = checksumManifest.get(fileObject);
          log.log(new ToolsLogRecord(ToolsLevel.INFO, "Found checksum in "
              + "the manifest for file object '" + fileObject.toString()
              + "': " + suppliedChecksum, product));
          if (!checksumInLabel.isEmpty()) {
            if (!suppliedChecksum.equals(checksumInLabel)) {
              log.log(new ToolsLogRecord(ToolsLevel.WARNING,
                  "Checksum in the manifest '" + suppliedChecksum
                  + "' does not match the checksum in the product label '"
                  + checksumInLabel + "' for file object '"
                  + fileObject.toString() + "'.", product));
                ++HarvestStats.numManifestChecksumsDiffInLabel;
            } else {
              log.log(new ToolsLogRecord(ToolsLevel.INFO,
                  "Checksum in the manifest '" + suppliedChecksum
                  + "' matches the checksum in the product label '"
                  + checksumInLabel + "' for file object '"
                  + fileObject.toString() + "'.", product));
                ++HarvestStats.numManifestChecksumsSameInLabel;
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
                "No checksum to compare against in the product label "
                + "for file object '"
                + fileObject.toString() + "'.", product));
            ++HarvestStats.numManifestChecksumsNotCheckedInLabel;
          }
          result = suppliedChecksum;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "No checksum found in the manifest for file object '"
              + fileObject.toString() + "'. ", product));
        }
      } else {
        if (!checksumInLabel.isEmpty()) {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Found checksum in the product label for file object '"
              + fileObject.toString() + "': " + checksumInLabel, product));
          result = checksumInLabel;
        }
      }
    }
    return result;
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

  /**
   * Set the file type mapping.
   *
   * @param fileTypes The file type mapping.
   */
  public void setFileTypes(FileTypes fileTypes) {
    this.fileTypes = fileTypes;
  }

  /**
   * Determine if there is a file type mapping.
   *
   * @return 'true' if there is one, 'false' otherwise.
   */
  private boolean containsFileTypes() {
    if (!fileTypes.getFileTypeMap().isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Get the file type of the given model value.
   *
   * @param modelValue The model value.
   *
   * @return The mapped file type or 'null' if it cannot be found.
   */
  private String getFileType(String modelValue) {
    for (FileTypeMap fileTypeMap : fileTypes.getFileTypeMap()) {
      for (String value : fileTypeMap.getModelValue()) {
        if (value.trim().equals(modelValue)) {
          return fileTypeMap.getValue();
        }
      }
    }
    return null;
  }
}
