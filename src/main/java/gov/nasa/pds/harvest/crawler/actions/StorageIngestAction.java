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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionPhases;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.file.FileObject;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;

/**
 * Class that will ingest registered products to the PDS Storage
 * Service.
 *
 * @author mcayanan
 *
 */
public class StorageIngestAction extends CrawlerAction {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
          StorageIngestAction.class.getName());

  /** The Storage Service Client object. */
  private XmlRpcFileManagerClient fmClient;

  /** The crawler action identifier. */
  private final static String ID = "StorageIngestAction";

  /** The crawler action description. */
  private final static String DESCRIPTION = "Ingests registered products "
    + "to the PDS Storage Service.";

  /** The product type name for the Storage Service. */
  private String productTypeName;

  /**
   * Constructor.
   *
   * @param storageServerUrl URL to the PDS storage server.
   *
   * @throws ConnectionException If there was an error connecting to the
   * Storage Service.
   */
  public StorageIngestAction(URL storageServerUrl)
  throws ConnectionException {
    fmClient = new XmlRpcFileManagerClient(storageServerUrl);
    fmClient.setDataTransfer(GenericFileManagerObjectFactory
        .getDataTransferServiceFromFactory(
            InPlaceDataTransferFactory.class.getName()));
    String []phases = {CrawlerActionPhases.PRE_INGEST};
    setPhases(Arrays.asList(phases));
    setId(ID);
    setDescription(DESCRIPTION);
    productTypeName = "ProductFile";
  }

  /**
   * Perform the action to ingest a product to the PDS Storage service.
   *
   * @param product The registered product.
   * @param metadata The metadata associated with the given product.
   *
   * @return true if the ingestion was successful, false otherwise.
   */
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    // create the product
    Product prod = new Product();
    String lidvid = metadata.getMetadata(Constants.LOGICAL_ID) + "::"
    + metadata.getMetadata(Constants.PRODUCT_VERSION);
    prod.setProductName(lidvid);
    prod.setProductStructure(Product.STRUCTURE_FLAT);
    try {
      prod.setProductType(fmClient.getProductTypeByName(productTypeName));
    } catch (RepositoryManagerException r) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING,
          "Unable to obtain product type: [" + productTypeName + "] "
          + "from File Manager at: [" + fmClient.getFileManagerUrl()
          + "]: Message: " + r.getMessage(), product));
      return false;
    }
    List<String> references = new Vector<String>();
    references.add(product.toURI().toString());
    // build refs and attach to product
    VersioningUtils.addRefsFromUris(prod, references);
    org.apache.oodt.cas.metadata.Metadata prodMet =
      new org.apache.oodt.cas.metadata.Metadata();
    prodMet.addMetadata("ProductClass", metadata.getMetadata(
        Constants.OBJECT_TYPE));

    // Are we doing a local/remote data transfer of the ingested product?
    try {
      String productId = fmClient.ingestProduct(prod, prodMet, true);
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Ingested '" + lidvid
          + "' to the Storage Service with product ID: " + productId, product)
      );
      metadata.addMetadata(Constants.STORAGE_SERVICE_PRODUCT_ID, productId);
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error occurred while "
          + "attempting to ingest into the file manager: "
          + ExceptionUtils.getRootCauseMessage(e), product));
      return false;
    }
    return true;
  }

  /**
   * Perform ingestion of a file object.
   *
   * @param product The file associated with the given file object.
   * @param fileObject The file object to ingest.
   * @param metadata The metadata associated with the given file.
   *
   * @return The storage service product identifier if an ingestion
   * was successful. If an error occurred, a null will be returned.
   */
  public String performAction(File product, FileObject fileObject,
      Metadata metadata) {
    Product prod = new Product();
    String lidvid = metadata.getMetadata(Constants.LOGICAL_ID) + ":"
    + fileObject.getName() + "::"
    + metadata.getMetadata(Constants.PRODUCT_VERSION);
    prod.setProductName(lidvid);
    prod.setProductStructure(Product.STRUCTURE_FLAT);
    try {
      prod.setProductType(fmClient.getProductTypeByName(productTypeName));
    } catch (RepositoryManagerException r) {
      log.log(new ToolsLogRecord(ToolsLevel.WARNING,
          "Unable to obtain product type: [" + productTypeName + "] "
          + "from File Manager at: [" + fmClient.getFileManagerUrl()
          + "]: Message: " + r.getMessage(), product));
      return null;
    }
    List<String> references = new Vector<String>();
    references.add(new File(fileObject.getLocation(), fileObject.getName())
    .toURI().toString());
    VersioningUtils.addRefsFromUris(prod, references);
    org.apache.oodt.cas.metadata.Metadata prodMet =
      new org.apache.oodt.cas.metadata.Metadata();
    prodMet.addMetadata("ProductClass", "Product_File_Repository");

    // Are we doing a local/remote data transfer of the ingested product?
    String productId = null;
    try {
      productId = fmClient.ingestProduct(prod, prodMet, true);
      log.log(new ToolsLogRecord(ToolsLevel.INFO, "Ingested '" + lidvid
          + "' to the Storage Service with product ID: " + productId, product)
      );
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error occurred while "
          + "attempting to ingest into the file manager: "
          + ExceptionUtils.getRootCauseMessage(e), product));
      return null;
    }
    return productId;

  }

  /**
   * Set the data transfer type.
   *
   * @param dataTransferType Either 'InPlaceProduct' or 'TransferProduct'.
   */
  public void setDataTransferType(String dataTransferType) {
    if ("TransferProduct".equalsIgnoreCase(dataTransferType)) {
      fmClient.setDataTransfer(GenericFileManagerObjectFactory
      .getDataTransferServiceFromFactory(RemoteDataTransferFactory.class
          .getName()));
    } else {
      fmClient.setDataTransfer(GenericFileManagerObjectFactory
          .getDataTransferServiceFromFactory(
              InPlaceDataTransferFactory.class.getName()));
    }
  }
}
