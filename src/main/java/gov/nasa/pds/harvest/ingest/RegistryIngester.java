// Copyright 2006-2015, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.ingest;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.file.FileSize;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;
import gov.nasa.pds.harvest.util.Utility;
import gov.nasa.pds.registry.client.RegistryClient;
import gov.nasa.pds.registry.client.SecurityContext;
import gov.nasa.pds.registry.exception.RegistryClientException;
import gov.nasa.pds.registry.exception.RegistryServiceException;
import gov.nasa.pds.registry.model.Association;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.PagedResponse;
import gov.nasa.pds.registry.model.Slot;
import gov.nasa.pds.registry.model.naming.DefaultIdentifierGenerator;
import gov.nasa.pds.registry.query.AssociationFilter;
import gov.nasa.pds.registry.query.ExtrinsicFilter;
import gov.nasa.pds.registry.query.RegistryQuery;

/**
 * Class that supports ingestion of PDS4 products into the PDS registry.
 *
 * @author mcayanan
 *
 */
public class RegistryIngester implements Ingester {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      RegistryIngester.class.getName());

  /** Password of the authorized user. */
  private String password;

  /** Username of the authorized user. */
  private String user;

  /** The registry package guid. */
  private String registryPackageGuid;

  /** The security context. */
  private SecurityContext securityContext;

  /** Indicates the number of concurrent registrations to make during
   *  batch mode.
   */
  private int batchMode;

  /** Registry Client object. */
  private RegistryClient client;

  /** UUID generator. */
  private DefaultIdentifierGenerator idGenerator;

  /** Batch Manager object associated with this Ingester. */
  private BatchManager batchManager;

  /**
   * Default constructor.
   *
   * @param packageGuid The GUID of the registry package to associate to
   * the products being registered.
   *
   */
  public RegistryIngester(String packageGuid) {
    this(packageGuid, null, null, null);
  }

  /**
    * Constructor.
    *
    * @param packageGuid The GUID of the registry package to associate to
    * the products being registered.
    * @param securityContext An object containing keystore information.
    * @param user An authorized user.
    * @param password The password associated with the user.
    */
  public RegistryIngester(String packageGuid, SecurityContext securityContext,
      String user, String password) {
    this.password = password;
    this.user = user;
    this.securityContext = securityContext;
    this.registryPackageGuid = packageGuid;
    this.batchMode = 0;
    this.batchManager = null;
    idGenerator = new DefaultIdentifierGenerator();
  }

  private RegistryClient getClient(URL registry)
      throws RegistryClientException {
    if (client == null) {
      client = new RegistryClient(registry.toString(),
          securityContext, user, password);
      if (registryPackageGuid != null) {
        client.setRegistrationPackageId(registryPackageGuid);
      }
      this.batchManager = new BatchManager(client);
    }
    return client;
  }

  /**
   * Method not used at this time.
   *
   */
  public boolean hasProduct(URL registry, File productFile)
  throws CatalogException {
      // No use for this method for now
    return false;
  }

  /**
   * Determines whether a product is already in the registry.
   *
   * @param registry The URL to the registry service.
   * @param lid The PDS4 logical identifier.
   *
   * @return 'true' if the logical identifier was found in the registry.
   * 'false' otherwise.
   *
   * @throws CatalogException exception ignored.
   */
  public boolean hasProduct(URL registry, String lid)
  throws CatalogException {
    try {
      RegistryClient client = getClient(registry);
      ExtrinsicObject extrinsic = client.getLatestObject(lid,
          ExtrinsicObject.class);
      return true;
    } catch (RegistryServiceException rse) {
      // Do nothing
    } catch (RegistryClientException rce) {
      throw new CatalogException(rce.getMessage());
    }
    return false;
  }

  /**
   * Determines whether a version of a product is already in the registry.
   *
   * @param registry The URL to the registry service.
   * @param lid The PDS4 logical identifier.
   * @param vid The version of the product.
   *
   * @return 'true' if the logical identifier and version was found in the
   * registry.
   *
   * @throws CatalogException If an error occurred while talking to the
   * ingester.
   */
  public boolean hasProduct(URL registry, String lid,
          String vid) throws CatalogException {
    RegistryClient client = null;
    try {
      client = getClient(registry);
    } catch (RegistryClientException rc) {
      throw new CatalogException(rc.getMessage());
    }
    ExtrinsicFilter filter = new ExtrinsicFilter.Builder().lid(lid)
    .build();
    RegistryQuery<ExtrinsicFilter> query = new RegistryQuery
    .Builder<ExtrinsicFilter>().filter(filter).build();
    try {
      PagedResponse<ExtrinsicObject> pr = client.getExtrinsics(query, null,
          null);
      if (pr.getNumFound() == 0) {
        return false;
      } else {
        for (ExtrinsicObject extrinsic : pr.getResults()) {
          for (Slot slot : extrinsic.getSlots()) {
            if (slot.getName().equals(Constants.PRODUCT_VERSION)
                && slot.getValues().contains(vid)) {
              return true;
            }
          }
        }
      }
    } catch (RegistryServiceException r) {
      throw new CatalogException(r.getMessage());
    }
    return false;
  }

  /**
   * Ingests the product into the registry.
   *
   * @param registry The URL to the registry service.
   * @param prodFile The PDS4 product file.
   * @param met The metadata to register.
   *
   * @return The URL of the registered product.
   * @throws IngestException If an error occurred while ingesting the
   * product.
   */
  public String ingest(URL registry, File prodFile, Metadata met)
  throws IngestException {
      String lid = met.getMetadata(Constants.LOGICAL_ID);
      String vid = met.getMetadata(Constants.PRODUCT_VERSION);
      String lidvid = lid + "::" + vid;
      try {
        if (!hasProduct(registry, lid, vid)) {
          ExtrinsicObject extrinsic = createProduct(met, prodFile);
          ingest(registry, prodFile, extrinsic);
          met.addMetadata(Constants.PRODUCT_GUID, extrinsic.getGuid());
          return extrinsic.getGuid();
        } else {
          ++HarvestStats.numProductsNotRegistered;
          String message = "Product already exists: " + lidvid;
          log.log(new ToolsLogRecord(ToolsLevel.WARNING, message,
              prodFile));
          throw new IngestException(message);
        }
      } catch (CatalogException c) {
        ++HarvestStats.numProductsNotRegistered;
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error while "
            + "checking for the existence of a registered product: "
            + c.getMessage(), prodFile));
        throw new IngestException(c.getMessage());
      }
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
    product.setGuid(idGenerator.getGuid());
    Set<Slot> slots = new HashSet<Slot>();
    Set metSet = metadata.getHashtable().entrySet();
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
        slots.addAll(metadata.getAllMetadata(Constants.SLOT_METADATA));
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
   * Ingest the given extrinsic object to the registry.
   *
   * @param registry The url of the registry.
   * @param extrinsic The extrinsic object to register.
   * @return The GUID of the registered extrinsic object.
   *
   * @throws RegistryServiceException If an error occurred while
   * attempting to register the extrinsic object.
   * @throws RegistryClientException If an error occurred initializing
   * the registry client.
   * @throws CatalogException
   */
  private void ingest(URL registry, File product, ExtrinsicObject extrinsic)
  throws CatalogException {
    boolean versionObject = false;
    if (hasProduct(registry, extrinsic.getLid())) {
      versionObject = true;
    }
    batchManager.cache(extrinsic, product, versionObject);
    if (batchManager.getCacheSize() >= batchMode) {
      batchManager.ingest();
    }
  }

  /**
   * Ingest the given file object to the registry.
   *
   * @param registry The url of the registry.
   * @param sourceFile The source file of the file object.
   * @param fileObject The file object to register.
   * @param met The file object metadata.
   * @return the guid of the registered file object.
   * @throws IngestException
   */
  public String ingest(URL registry, File sourceFile, FileObject fileObject,
      Metadata met) throws IngestException {
    Metadata fileObjectMet = createFileObjectMetadata(fileObject, met);
    ExtrinsicObject fileProduct = createProduct(fileObjectMet, sourceFile);
    String lid = fileObjectMet.getMetadata(Constants.LOGICAL_ID);
    String vid = fileObjectMet.getMetadata(Constants.PRODUCT_VERSION);
    String lidvid = lid + "::" + vid;
    try {
      if (!hasProduct(registry, lid, vid)) {
        ingest(registry, sourceFile, fileProduct);
      } else {
        throw new IngestException("Product already exists: " + lidvid);
      }
    } catch (CatalogException ce) {
      throw new IngestException("Error while checking for the existence of a "
        + "registered product: " + ce.getMessage());
    }
    return fileProduct.getGuid();
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
      lid += ":" + filename;
    } else {
      lid += ":" + fileObject.getName();
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
    for (Iterator i = sourceMet.getHashtable().entrySet().iterator();
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
      metadata.addMetadata(Constants.SLOT_METADATA, slots);
    }
    return metadata;
  }

  /**
   * Ingests an association to the registry.
   *
   * @param registry The url of the registry.
   * @param sourceFile The source file.
   * @param association The association to register.
   * @param targetReference The lidvid of the target reference.
   * @return A guid if the ingestion was successful.
   *
   * @throws IngestException If an error occurred while ingesting
   * the association.
   */
  public String ingest(URL registry, File sourceFile, Association association,
      String targetReference) throws IngestException {
    try {
      if (!hasAssociation(registry, association)) {
        String guid = "";
        guid = idGenerator.getGuid();
        association.setGuid(guid);
        batchManager.cache(association, sourceFile, false);
        if (batchManager.getCacheSize() >= batchMode) {
          batchManager.ingest();
        }
        return guid;
      } else {
        String message = "Association to " + targetReference + ", with \'"
        + association.getAssociationType() + "\' association type, already "
        + "exists in the registry.";
        throw new IngestException(message);
      }
    } catch (RegistryClientException e) {
      throw new IngestException("Problem registering association to "
          + targetReference + ": " + e.getMessage());
    }
  }

  /**
   * Determines if an association already exists in the registry.
   *
   * @param association The association.
   *
   * @return true if the association exists.
   * @throws RegistryClientException
   * @throws RegistryServiceException
   */
  public boolean hasAssociation(URL registry, Association association)
  throws RegistryClientException {
    boolean result = false;
    AssociationFilter filter = new AssociationFilter.Builder()
    .targetObject(association.getTargetObject())
    .associationType(association.getAssociationType()).build();
    RegistryQuery<AssociationFilter> query = new RegistryQuery
    .Builder<AssociationFilter>().filter(filter).build();
    try {
      RegistryClient client = getClient(registry);
      PagedResponse<Association> responses = client.getAssociations(
        query, 1, 10);
      if (responses.getNumFound() != 0) {
        for (Association response : responses.getResults()) {
          if (response.getSourceObject().equals(
              association.getSourceObject())) {
            result = true;
            break;
          }
        }
      }
    } catch (RegistryServiceException r) {
      //Do nothing
    }
    return result;
  }

  /**
   * Gets the extrinsic object with the given LID and VID.
   *
   * @param registry The registry url.
   * @param lid The LID to look up in the registry.
   * @param version The version of the product to look up.
   * @return The extrinsic object that matches the given LID and VID.
   * @throws RegistryClientException
   */
  public ExtrinsicObject getExtrinsic(URL registry, String lid,
      String version) throws IngestException {
    ExtrinsicObject result = null;
    ExtrinsicFilter filter = new ExtrinsicFilter.Builder().lid(lid)
    .build();
    RegistryQuery<ExtrinsicFilter> query = new RegistryQuery
    .Builder<ExtrinsicFilter>().filter(filter).build();
    try {
      RegistryClient client = getClient(registry);
      PagedResponse<ExtrinsicObject> pr = client.getExtrinsics(query,
        null, null);
      if (pr.getNumFound() != 0) {
        for (ExtrinsicObject extrinsic : pr.getResults()) {
          for (Slot slot : extrinsic.getSlots()) {
            if (slot.getName().equals(Constants.PRODUCT_VERSION)
                && slot.getValues().contains(version)) {
              result = extrinsic;
            }
          }
        }
      }
    } catch (RegistryServiceException rse) {
      //Ignore. Nothing found.
    } catch (RegistryClientException rce) {
      throw new IngestException(rce.getMessage());
    }
    return result;
  }

  /**
   * Method not implemented at this time.
   *
   */
  public String ingest(URL fmUrl, File prodFile, MetExtractor extractor,
          File metConfFile) throws IngestException {
    //No need for this method at this time
    return null;
  }

  /**
   * Method not implemented at this time.
   *
   */
  public void ingest(URL fmUrl, List<String> prodFiles,
          MetExtractor extractor, File metConfFile)
          throws IngestException {
      //No need for this method at this time
  }

  /**
   * Sets the number of concurrent registrations to make
   * during batch mode.
   *
   * @param value An integer value.
   */
  public void setBatchMode(int value) {
    this.batchMode = value;
  }

  /**
   * @return The Batch Manager object.
   */
  public BatchManager getBatchManager() {
    return this.batchManager;
  }
}
