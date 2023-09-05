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

package gov.nasa.pds.harvest.search.crawler.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.tiny.TinyElementImpl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionPhases;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.file.FileObject;
import gov.nasa.pds.harvest.search.file.FileSize;
import gov.nasa.pds.harvest.search.file.MD5Checksum;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.policy.FileTypeMap;
import gov.nasa.pds.harvest.search.policy.FileTypes;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;
import gov.nasa.pds.harvest.search.util.PointerStatementFinder;
import gov.nasa.pds.harvest.search.util.Utility;
import gov.nasa.pds.harvest.search.util.XMLExtractor;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.Slot;
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
   */
  public FileObjectRegistrationAction() {
      super();
      String []phases = {CrawlerActionPhases.PRE_INGEST.getName()};
      setPhases(Arrays.asList(phases));
      setId(ID);
      setDescription(DESCRIPTION);
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
    List<ExtrinsicObject> fileProducts = new ArrayList<ExtrinsicObject>();
    for (FileObject fileObject : fileObjectEntries) {
      String lid = metadata.getMetadata(Constants.LOGICAL_ID) + ":"
      + fileObject.getName().toLowerCase();
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
        Metadata fileObjectMet = createFileObjectMetadata(fileObject, metadata);
        ExtrinsicObject fileProduct = createProduct(fileObjectMet, product);
        fileProducts.add(fileProduct);
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            ExceptionUtils.getRootCauseMessage(e),
            product));
        e.printStackTrace();
        ++HarvestSolrStats.numAncillaryProductsNotRegistered;
        throw new CrawlerActionException(e.getMessage());
      }
    }
    if (!fileProducts.isEmpty()) {
    	for (ExtrinsicObject fileProduct : fileProducts) {
    		metadata.addMetadata("file_ref", fileProduct.getSlot("file_ref").getValues());
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

  /**
   * Create the Product object.
   *
   * @param metadata A class representation of the metdata.
   *
   * @return A Product object.
   */
  private ExtrinsicObject createProduct(Metadata metadata, File prodFile) {
    ExtrinsicObject product = new ExtrinsicObject();
//    product.setGuid(idGenerator.getGuid());
    Set<Slot> slots = new HashSet<Slot>();
    Set metSet = metadata.getHashTable().entrySet();
    for (Iterator i = metSet.iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String key = entry.getKey().toString();
      if (key.equals(Constants.REFERENCES)
          || key.equals(Constants.INCLUDE_PATHS)) {
        continue;
      }
      if (key.equals(Constants.LOGICAL_ID)) {
        product.setLid(metadata.getMetadata(Constants.LOGICAL_ID));
      } else if (key.equals(Constants.PRODUCT_VERSION)) {
        slots.add(new Slot(Constants.PRODUCT_VERSION,
            Arrays.asList(new String[]{
                metadata.getMetadata(Constants.PRODUCT_VERSION)}
            )));
      } else if (key.equals(Constants.OBJECT_TYPE)) {
        product.setObjectType(metadata.getMetadata(
             Constants.OBJECT_TYPE));
      } else if (key.equals(Constants.TITLE)) {
        product.setName(metadata.getMetadata(Constants.TITLE));
      } else if (key.equals(Constants.SLOT_METADATA)) {
    	  for (String k : metadata.getKeys(Constants.SLOT_METADATA)) {
    		  slots.add(new Slot(k, metadata.getAllMetadata(k)));
    	  }
      } else {
        log.log(new ToolsLogRecord(ToolsLevel.WARNING,
            "Creating unexpected slot: " + key, prodFile));
        List<String> values = new ArrayList<String>();
        if (metadata.isMultiValued(key)) {
          values.addAll(metadata.getAllMetadata(key));
        } else {
          values.add(metadata.getMetadata(key));
        }
        slots.add(new Slot(key, values));
      }
      product.setSlots(slots);
    }
  
    if (log.getParent().getHandlers()[0].getLevel().intValue()
        <= ToolsLevel.DEBUG.intValue()) {
      try {
      log.log(new ToolsLogRecord(ToolsLevel.DEBUG,
        "Extrinsic object contents: \n" + Utility.toXML(product)));
      } catch (JAXBException je) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, je.getMessage()));
      }
    }
    return product;
  }
  
  
  /**
   * Create a metadata object to associate with the file object.
   *
   * @param fileObject The file object.
   * @param sourceMet The metadata of the source file.
   *
   * @return The metadata associated with the given file object.
   */
  private Metadata createFileObjectMetadata(FileObject fileObject,
      Metadata sourceMet) {
    Metadata metadata = new Metadata();
    List<Slot> slots = new ArrayList<Slot>();
    String lid = sourceMet.getMetadata(Constants.LOGICAL_ID);
    String extension = FilenameUtils.getExtension(fileObject.getName());
    if ("xml".equalsIgnoreCase(extension)) {
      String filename = FilenameUtils.removeExtension(fileObject.getName());
      filename += "_xml";
      lid += ":" + filename.toLowerCase();
    } else {
      lid += ":" + fileObject.getName().toLowerCase();
    }
    metadata.addMetadata(Constants.LOGICAL_ID, lid);
    metadata.addMetadata(Constants.TITLE, FilenameUtils.getBaseName(
        fileObject.getName()));
    metadata.addMetadata(Constants.OBJECT_TYPE,
        Constants.FILE_OBJECT_PRODUCT_TYPE);

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

    if ( (fileObject.getFileType() != null
        && (!fileObject.getFileType().isEmpty()))) {
      slots.add(new Slot(Constants.FILE_TYPE,
          Arrays.asList(new String[]{fileObject.getFileType()})));
    }

    slots.add(new Slot(Constants.CREATION_DATE_TIME,
        Arrays.asList(new String[]{fileObject.getCreationDateTime()})));

    if (fileObject.getStorageServiceProductId() != null) {
      slots.add(new Slot(Constants.STORAGE_SERVICE_PRODUCT_ID,
          Arrays.asList(new String[]{fileObject.getStorageServiceProductId()})));
    }
    if (!fileObject.getAccessUrls().isEmpty()) {
      slots.add(new Slot(Constants.ACCESS_URLS, fileObject.getAccessUrls()));
    }
    for (Iterator i = sourceMet.getHashTable().entrySet().iterator();
    i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String key = entry.getKey().toString();
      if (key.equals("dd_version_id")
          || key.equals("std_ref_version_id")) {
        slots.add(new Slot(key, Arrays.asList(
            new String[]{sourceMet.getMetadata(key)})));
      } else if (key.equals(Constants.PRODUCT_VERSION)) {
        metadata.addMetadata(Constants.PRODUCT_VERSION,
            sourceMet.getMetadata(Constants.PRODUCT_VERSION));
      }
    }
    if (!slots.isEmpty()) {
    	Metadata submeta = new Metadata();
    	for (Slot slot : slots) {
    		submeta.addMetadata(slot.getName(), slot.getValues());
    	}
      metadata.addMetadata(Constants.SLOT_METADATA, submeta);
    }
    return metadata;
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
      ++HarvestSolrStats.numAncillaryProductsNotRegistered;
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
          ++HarvestSolrStats.numAncillaryProductsNotRegistered;
        }
      } catch (Exception e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error "
            + "occurred while generating checksum for " + xincludeFile.getName()
            + ": " + e.getMessage(), product.toString()));
        ++HarvestSolrStats.numAncillaryProductsNotRegistered;
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
        ++HarvestSolrStats.numAncillaryProductsNotRegistered;
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
      ++HarvestSolrStats.numAncillaryProductsNotRegistered;
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
    List<File> uniqueFiles = new ArrayList<File>();
    String basePath = product.getParent();
    List<PointerStatement> pointers = PointerStatementFinder.find(label);
    for (PointerStatement ps : pointers) {
      for (FileReference fileRef : ps.getFileRefs()) {
        File file = resolvePath(fileRef.getPath(), basePath, includePaths);
        try {
          if (file != null) {
            if (!file.getName().equals(product.getName())
                && !uniqueFiles.contains(file)) {
              log.log(new ToolsLogRecord(ToolsLevel.INFO, "Capturing file "
                + "object metadata for " + file.getName(), product));
              long size = file.length();
              String creationDateTime = format.format(new Date(
                file.lastModified()));
              String checksum = handleChecksum(product, file);
              results.add(new FileObject(file.getName(), file.getParent(),
                  new FileSize(size, Constants.BYTE),
                  creationDateTime, checksum, "Observation"));
              uniqueFiles.add(file);
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "File object not "
                + "found: " + fileRef.getPath(), product.toString()));
            ++HarvestSolrStats.numAncillaryProductsNotRegistered;
          }
        } catch (Exception e) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error occurred "
              + "while calculating checksum for " + file.getName() + ": ",
              product));
          ++HarvestSolrStats.numAncillaryProductsNotRegistered;
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
            ++HarvestSolrStats.numGeneratedChecksumsDiffInManifest;
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Generated checksum '" + generatedChecksum
              + "' matches the supplied checksum '" + suppliedChecksum
              + "' in the manifest for file object '" + fileObject.toString()
              + "'.", product));
            ++HarvestSolrStats.numGeneratedChecksumsSameInManifest;
          }
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "No checksum found in the manifest for file object '"
              + fileObject.toString() + "'.", product));
          ++HarvestSolrStats.numGeneratedChecksumsNotCheckedInManifest;
        }
      }
      if (!checksumInLabel.isEmpty()) {
        if (!generatedChecksum.equals(checksumInLabel)) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING,
              "Generated checksum '" + generatedChecksum
              + "' does not match supplied checksum '"
              + checksumInLabel + "' in the product label for file object '"
              + fileObject.toString() + "'.", product));
            ++HarvestSolrStats.numGeneratedChecksumsDiffInLabel;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Generated checksum '" + generatedChecksum
              + "' matches the supplied checksum '" + checksumInLabel
              + "' in the product label for file object '"
              + fileObject.toString() + "'.", product));
            ++HarvestSolrStats.numGeneratedChecksumsSameInLabel;
        }
      } else {
        log.log(new ToolsLogRecord(ToolsLevel.INFO,
            "No checksum to compare against in the product label "
            + "for file object '" + fileObject.toString() + "'.", product));
        ++HarvestSolrStats.numGeneratedChecksumsNotCheckedInLabel;
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
                ++HarvestSolrStats.numManifestChecksumsDiffInLabel;
            } else {
              log.log(new ToolsLogRecord(ToolsLevel.INFO,
                  "Checksum in the manifest '" + suppliedChecksum
                  + "' matches the checksum in the product label '"
                  + checksumInLabel + "' for file object '"
                  + fileObject.toString() + "'.", product));
                ++HarvestSolrStats.numManifestChecksumsSameInLabel;
            }
          } else {
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
                "No checksum to compare against in the product label "
                + "for file object '"
                + fileObject.toString() + "'.", product));
            ++HarvestSolrStats.numManifestChecksumsNotCheckedInLabel;
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
