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
import java.util.List;
import java.util.logging.Logger;

import net.sf.saxon.tinytree.TinyElementImpl;

import org.apache.commons.lang.exception.ExceptionUtils;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.file.MD5Checksum;
import gov.nasa.pds.harvest.ingest.RegistryIngester;
import gov.nasa.pds.harvest.inventory.ReferenceEntry;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
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
    String generatedChecksum = "";
    // Create a file object of the label file
    String lastModified = format.format(new Date(product.lastModified()));
    try {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file information "
          + "for " + product.getName(), product));
      generatedChecksum = MD5Checksum.getMD5Checksum(product.toString());
      if (Constants.suppliedChecksums.containsKey(product)) {
        String suppliedChecksum = Constants.suppliedChecksums.get(product);
        if (!suppliedChecksum.equals(generatedChecksum)) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Generated checksum '" + generatedChecksum
              + "' does not match supplied checksum '"
              + suppliedChecksum + "' in the inventory.", product));
          ++HarvestStats.numChecksumsDifferent;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Generated checksum '" + generatedChecksum
              + "' matches the supplied checksum '" + suppliedChecksum
              + "' in the inventory.", product));
          ++HarvestStats.numChecksumsSame;
        }
      } else {
        if (!Constants.suppliedChecksums.isEmpty()) {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "No checksum supplied in the inventory for this product label.",
              product));
        }
        ++HarvestStats.numChecksumsNotChecked;
      }
      FileObject fileObject = new FileObject(product.getName(),
          product.getParent(), product.length(),
          lastModified, generatedChecksum);
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
    // Search for File_Area_*/File tags within the product label
    List<TinyElementImpl> fileObjects = extractor.getNodesFromDoc(
        Constants.coreXpathsMap.get(Constants.FILE_OBJECTS));
    for (TinyElementImpl file : fileObjects) {
      String fileLocation = product.getParent();
      String name = "";
      long size = -1;
      String checksum = "";
      String creationDateTime = "";
      List<TinyElementImpl> children = extractor.getNodesFromItem("*", file);
      for (TinyElementImpl child : children) {
        if ("file_name".equals(child.getLocalPart())) {
          name = child.getStringValue();
        } else if ("file_size".equals(child.getLocalPart())) {
          size = Long.parseLong(child.getStringValue());
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
          }
          if (creationDateTime.isEmpty()) {
            creationDateTime = format.format(new Date(f.lastModified()));
          }
          try {
            generatedChecksum = MD5Checksum.getMD5Checksum(f.toString());
            if (!checksum.isEmpty()) {
              if (!checksum.equals(generatedChecksum)) {
                log.log(new ToolsLogRecord(ToolsLevel.WARNING,
                    "Generated checksum '" + generatedChecksum
                    + "' does not match supplied checksum '" + checksum
                    + "' for file object '" + name + "'.", product));
                ++HarvestStats.numChecksumsDifferent;
              } else {
                log.log(new ToolsLogRecord(ToolsLevel.INFO,
                    "Generated checksum '" + generatedChecksum + "' matches "
                    + "the supplied checksum '" + checksum
                    + "' for file object '" + name + "'.", product));
                ++HarvestStats.numChecksumsSame;
              }
            } else {
              log.log(new ToolsLogRecord(ToolsLevel.INFO,
                  "No checksum supplied for file object '" + name
                  + "' in the product label.", product));
              ++HarvestStats.numChecksumsNotChecked;
            }
          } catch (Exception e) {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
                + "occurred while calculating checksum for '" + name + "': "
                + e.getMessage(), product.toString()));
            throw new Exception("Missing checksum");
          }
          results.add(new FileObject(f.getName(), f.getParent(), size,
              creationDateTime, generatedChecksum));
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
      FileObject fileObject = new FileObject(product.getName(),
          product.getParent(), product.length(),
          lastModified, MD5Checksum.getMD5Checksum(product.toString()));
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
              String checksum = MD5Checksum.getMD5Checksum(file.toString());
              results.add(new FileObject(file.getName(), file.getParent(),
                  size, creationDateTime, checksum));
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
}
