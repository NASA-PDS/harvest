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

import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionPhases;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory;
import org.apache.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.file.FileObject;
import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;

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
    String []phases = {CrawlerActionPhases.PRE_INGEST.getName()};
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
