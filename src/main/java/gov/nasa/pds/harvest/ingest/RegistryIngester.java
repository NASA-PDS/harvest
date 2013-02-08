// Copyright 2006-2010, by the California Institute of Technology.
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
import gov.nasa.pds.harvest.crawler.metadata.ObservingSystemComponent;
import gov.nasa.pds.harvest.file.FileObject;
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
      RegistryClient client = new RegistryClient(registry.toString(),
          securityContext, user, password);
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
      client = new RegistryClient(registry.toString(),
          securityContext, user, password);
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
      String guid = "";
      String lid = met.getMetadata(Constants.LOGICAL_ID);
      String vid = met.getMetadata(Constants.PRODUCT_VERSION);
      String lidvid = lid + "::" + vid;
      try {
        if (!hasProduct(registry, lid, vid)) {
          ExtrinsicObject extrinsic = createProduct(met);
          guid = ingest(registry, extrinsic);
          log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
              "Successfully registered product: " + lidvid, prodFile));
          log.log(new ToolsLogRecord(ToolsLevel.INFO,
              "Product has the following GUID: " + guid, prodFile));
          met.addMetadata(Constants.PRODUCT_GUID, guid);
          ++HarvestStats.numProductsRegistered;
          HarvestStats.addProductType(extrinsic.getObjectType(), prodFile);
          return guid;
        } else {
          ++HarvestStats.numProductsNotRegistered;
          String message = "Product already exists: " + lidvid;
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, message,
              prodFile));
          throw new IngestException(message);
        }
      } catch (RegistryServiceException r) {
        ++HarvestStats.numProductsNotRegistered;
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            r.getMessage(), prodFile));
        throw new IngestException(r.getMessage());
      } catch (CatalogException c) {
        ++HarvestStats.numProductsNotRegistered;
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error while "
            + "checking for the existence of a registered product: "
            + c.getMessage(), prodFile));
        throw new IngestException(c.getMessage());
      } catch (RegistryClientException rce) {
        ++HarvestStats.numProductsNotRegistered;
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
            "Error while initializing RegistryClient: " + rce.getMessage()));
        throw new IngestException(rce.getMessage());
      }
  }

    /**
     * Create the Product object.
     *
     * @param metadata A class representation of the metdata.
     *
     * @return A Product object.
     */
  private ExtrinsicObject createProduct(Metadata metadata) {
    ExtrinsicObject product = new ExtrinsicObject();
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
      } else if (key.equals(Constants.OBJECT_TYPE)) {
         product.setObjectType(metadata.getMetadata(
             Constants.OBJECT_TYPE));
      } else if (key.equals(Constants.TITLE)) {
         product.setName(metadata.getMetadata(Constants.TITLE));
      } else if (key.equals(Constants.OBSERVING_SYSTEM_COMPONENT_KEY)) {
        for ( ObservingSystemComponent component :
          (List<ObservingSystemComponent>) metadata.getAllMetadata(
              Constants.OBSERVING_SYSTEM_COMPONENT_KEY) ) {
          List<String> values = new ArrayList<String>();
          values.add(component.getName());
          values.add(component.getType());
          if (component.hasReference()) {
            values.add(component.getReference());
          }
          slots.add(new Slot(key, values));
        }
      } else {
         List<String> values = new ArrayList<String>();
         if (metadata.isMultiValued(key)) {
           values.addAll(metadata.getAllMetadata(key));
         } else {
           values.add(metadata.getMetadata(key));
         }
         slots.add(new Slot(key, values));
      }
    }
    product.setSlots(slots);
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
  private String ingest(URL registry, ExtrinsicObject extrinsic)
  throws RegistryServiceException, RegistryClientException, CatalogException {
    String guid = "";
    RegistryClient client = new RegistryClient(registry.toString(),
        securityContext, user, password);
    if (!registryPackageGuid.isEmpty()) {
      client.setRegistrationPackageId(registryPackageGuid);
    }
    if (hasProduct(registry, extrinsic.getLid())) {
      guid = client.versionObject(extrinsic);
    } else {
      guid = client.publishObject(extrinsic);
    }
    return guid;
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
    ExtrinsicObject fileProduct = createProduct(fileObjectMet);
    String guid = "";
    String lid = fileObjectMet.getMetadata(Constants.LOGICAL_ID);
    String vid = fileObjectMet.getMetadata(Constants.PRODUCT_VERSION);
    String lidvid = lid + "::" + vid;
    try {
      if (!hasProduct(registry, lid, vid)) {
        guid = ingest(registry, fileProduct);
      } else {
        throw new IngestException("Product already exists: " + lidvid);
      }
    } catch (RegistryServiceException rse) {
      throw new IngestException(rse.getMessage());
    } catch (CatalogException ce) {
      throw new IngestException("Error while checking for the existence of a "
        + "registered product: " + ce.getMessage());
    } catch (RegistryClientException rce) {
      throw new IngestException("Error while initializing RegistryClient: "
          + rce.getMessage());
    }
    return guid;
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
    metadata.addMetadata(Constants.LOGICAL_ID, sourceMet.getMetadata(
        Constants.LOGICAL_ID) + ":" + fileObject.getName());
    metadata.addMetadata(Constants.TITLE, FilenameUtils.getBaseName(
        fileObject.getName()));
    metadata.addMetadata(Constants.OBJECT_TYPE,
        Constants.FILE_OBJECT_PRODUCT_TYPE);
    metadata.addMetadata(Constants.FILE_NAME, fileObject.getName());
    metadata.addMetadata(Constants.FILE_LOCATION, fileObject.getLocation());
    metadata.addMetadata(Constants.FILE_SIZE, Long.toString(
        fileObject.getSize()));
    if ( (fileObject.getChecksum()) != null
        && (!fileObject.getChecksum().isEmpty()) ) {
      metadata.addMetadata(Constants.MD5_CHECKSUM, fileObject.getChecksum());
    }
    metadata.addMetadata(Constants.CREATION_DATE_TIME,
        fileObject.getCreationDateTime());
    if (fileObject.getStorageServiceProductId() != null) {
      metadata.addMetadata(Constants.STORAGE_SERVICE_PRODUCT_ID,
        fileObject.getStorageServiceProductId());
    }
    if (!fileObject.getAccessUrls().isEmpty()) {
      metadata.addMetadata(Constants.ACCESS_URLS, fileObject.getAccessUrls());
    }
    for (Iterator i = sourceMet.getHashtable().entrySet().iterator();
    i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      String key = entry.getKey().toString();
      if (key.equals("dd_version_id")
          || key.equals("std_ref_version_id")
          || key.equals(Constants.PRODUCT_VERSION)) {
        metadata.addMetadata(key, sourceMet.getMetadata(key));
      }
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
    String guid = "";
    try {
      RegistryClient client = new RegistryClient(registry.toString(),
          securityContext, user, password);
      if (!registryPackageGuid.isEmpty()) {
        client.setRegistrationPackageId(registryPackageGuid);
      }
      if (!hasAssociation(registry, association)) {
        guid = client.publishObject(association);
      } else {
        String message = "Association to " + targetReference + ", with \'"
        + association.getAssociationType() + "\' association type, already "
        + "exists in the registry.";
        throw new IngestException(message);
      }
    } catch (Exception e) {
      throw new IngestException("Problem registering association to "
         + targetReference + ": " + e.getMessage());
    }
    return guid;
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
    .sourceObject(association.getSourceObject())
    .targetObject(association.getTargetObject())
    .associationType(association.getAssociationType()).build();
    RegistryQuery<AssociationFilter> query = new RegistryQuery
    .Builder<AssociationFilter>().filter(filter).build();
    try {
      RegistryClient client = new RegistryClient(registry.toString(),
          securityContext, user, password);
      PagedResponse<Association> response = client.getAssociations(
        query, 1, 10);
      if (response.getNumFound() != 0) {
        result = true;
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
      RegistryClient client = new RegistryClient(registry.toString(),
          securityContext, user, password);
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
}
