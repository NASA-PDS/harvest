// Copyright 2006-2019, by the California Institute of Technology.
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
// $Id: SearchIngester.java 14165 2015-07-09 18:20:14Z mcayanan $
package gov.nasa.pds.harvest.search.ingest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;

import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;
import gov.nasa.pds.harvest.search.util.Utility;
import gov.nasa.pds.registry.client.SecurityContext;
import gov.nasa.pds.registry.model.naming.DefaultIdentifierGenerator;


/**
 * Class that supports ingestion of PDS4 product labels as a 
 * blob into the PDS Search Service.
 *
 * @author mcayanan
 *
 */
public class SearchIngester implements Ingester {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      SearchIngester.class.getName());

  /** Password of the authorized user. */
  private String password;

  /** Username of the authorized user. */
  private String user;

  /** The registry package guid. */
  private String registryPackageGuid;

  /** The security context. */
  private SecurityContext securityContext;

  /** Solr Client object. */
  private SolrClient client;

  /** UUID generator. */
  private DefaultIdentifierGenerator idGenerator;

  /**
   * Default constructor.
   *
   * @param packageGuid The GUID of the registry package to associate to
   * the products being registered.
   *
   */
  public SearchIngester(String packageGuid) {
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
  public SearchIngester(String packageGuid, SecurityContext securityContext,
      String user, String password) {
    this.password = password;
    this.user = user;
    this.securityContext = securityContext;
    this.registryPackageGuid = packageGuid;
    idGenerator = new DefaultIdentifierGenerator();
  }

  private SolrClient getClient(URL url) throws SolrServerException, IOException {
    if (client == null) {
      client = new HttpSolrClient.Builder(url.toString()).build();
      try {
        createSystemCollection(client);
      } catch (SolrServerException se) {
        throw new SolrServerException("Error creating .system collection: "
            + se.getMessage());
      }
    }
    return client;
  }

  /**
   * Creates the .system collection if it does not exist.
   * 
   * @param client The SolrClient.
   * 
   * @throws SolrServerException If an error occurred talking to the Solr 
   * Server.
   * @throws IOException If an IO error occurred.
   */
  private void createSystemCollection(SolrClient client)
      throws SolrServerException, IOException {
    List<String> collections = CollectionAdminRequest.listCollections(client);
    if (!collections.contains(".system")) {
      log.log(new ToolsLogRecord(ToolsLevel.INFO, 
          "Creating the .system collection with 2 shards and 2 replicas"));
      CollectionAdminRequest.Create req = 
          CollectionAdminRequest.createCollection(".system", 2, 2);
      req.process(client);
    }
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
    SolrClient client = null;
    boolean foundProduct = false;
    try {
      client = getClient(registry);
      SolrQuery query = new SolrQuery("blobName:" + Utility.createBlobName(lid));
      QueryResponse response = client.query(".system", query);
      SolrDocumentList documents = response.getResults();
      if (documents.getNumFound() != 0) {
        foundProduct = true;
      }
    } catch (Exception e) {
      throw new CatalogException("Error while trying to find blob with blobName '"
          + Utility.createBlobName(lid) + "': " + e.getMessage());
    }
    return foundProduct;
  }

  /**
   * Ingests the product into the registry.
   *
   * @param searchUrl The URL to the Search Service.
   * @param prodFile The PDS4 product file.
   * @param met The metadata to register.
   *
   * @return The URL of the registered product.
   * @throws IngestException If an error occurred while ingesting the
   * product.
   */
  public String ingest(URL searchUrl, File prodFile, Metadata met)
  throws IngestException {
      String lid = met.getMetadata(Constants.LOGICAL_ID);
      String vid = met.getMetadata(Constants.PRODUCT_VERSION);
      String lidvid = lid + "::" + vid;
      try {
        if (!hasProduct(searchUrl, lid, vid)) {
          String endPoint = "/.system/blob/" + Utility.createBlobName(lid);
          ContentStreamUpdateRequest up = new ContentStreamUpdateRequest(endPoint);
          up.addFile(prodFile, MediaType.APPLICATION_OCTET_STREAM);
          up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
          NamedList<Object> list = getClient(searchUrl).request(up);
          String registeredBlob = searchUrl + endPoint;
          log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
              "Successfully registered product: " + lidvid + ". "
                  + "Label file blob can be found here: " + registeredBlob,
               prodFile));
          ++HarvestSolrStats.numProductsRegistered;
          return Utility.createBlobName(lid);
        } else {
          ++HarvestSolrStats.numProductsNotRegistered;
          String message = "Product already exists: " + lidvid;
          log.log(new ToolsLogRecord(ToolsLevel.WARNING, message,
              prodFile));
          throw new IngestException(message);
        }
      } catch (CatalogException c) {
        ++HarvestSolrStats.numProductsNotRegistered;
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error while "
            + "checking for the existence of a registered product: "
            + c.getMessage(), prodFile));
        throw new IngestException(c.getMessage());
      } catch (IOException io) {
        // TODO Auto-generated catch block
        io.printStackTrace();
        throw new IngestException(io.getMessage());
      } catch (SolrServerException se) {
        // TODO Auto-generated catch block
        throw new IngestException(se.getMessage());
      }
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
