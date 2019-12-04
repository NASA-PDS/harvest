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

package gov.nasa.pds.harvest.search.ingest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.pdfbox.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;
import org.json.XML;

import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;

import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;
import gov.nasa.pds.harvest.search.util.TransactionManager;


/**
 * Class that supports ingestion of PDS4 product labels as a blob into the PDS
 * Search Service.
 *
 * @author mcayanan
 *
 */
public class SearchIngester implements Ingester {
	private static Logger log = Logger.getLogger(SearchIngester.class.getName());

	private SolrClient client;


	/**
	 * Default constructor.
	 */
	public SearchIngester() {
	}


	private SolrClient getClient(URL url) throws SolrServerException, IOException {
		if (client == null) {
			client = new HttpSolrClient.Builder(url.toString()).build();
			try {
				createCollections(client);
			} catch (SolrServerException se) {
				throw new SolrServerException("Error creating .system and/or xpath collection: " + se.getMessage());
			}
		}
		return client;
	}

	/**
	 * Creates the .system and xpath collections if it does not exist.
	 * 
	 * @param client The SolrClient.
	 * 
	 * @throws SolrServerException If an error occurred talking to the Solr Server.
	 * @throws IOException         If an IO error occurred.
	 */
	private void createCollections(SolrClient client) throws SolrServerException, IOException 
	{
		List<String> collections = CollectionAdminRequest.listCollections(client);

		if (!collections.contains("xpath")) 
		{
			log.log(new ToolsLogRecord(ToolsLevel.INFO, "Creating the xpath collection with 1 shards and 1 replicas"));
			CollectionAdminRequest.Create req = CollectionAdminRequest.createCollection("xpath", 1, 1);
			req.process(client);
		}
	}

	/**
	 * Method not used at this time.
	 *
	 */
	public boolean hasProduct(URL registry, File productFile) throws CatalogException {
		// No use for this method for now
		return false;
	}

	/**
	 * Determines whether a product is already in the registry.
	 *
	 * @param registry The URL to the registry service.
	 * @param lid      The PDS4 logical identifier.
	 *
	 * @return 'true' if the logical identifier was found in the registry. 'false'
	 *         otherwise.
	 *
	 * @throws CatalogException exception ignored.
	 */
	public boolean hasProduct(URL registry, String lid) throws CatalogException {
		return false;
	}

	/**
	 * Determines whether a version of a product is already in the registry.
	 *
	 * @param registry The URL to the registry service.
	 * @param lid      The PDS4 logical identifier.
	 * @param vid      The version of the product.
	 *
	 * @return 'true' if the logical identifier and version was found in the
	 *         registry.
	 *
	 * @throws CatalogException If an error occurred while talking to the ingester.
	 */
	public boolean hasProduct(URL registry, String lid, String vid) throws CatalogException 
	{
		SolrClient client = null;
		String lidvid = lid + "::" + vid;

		try 
		{
			client = getClient(registry);

			SolrQuery query = new SolrQuery("lidvid:\"" + lidvid + "\"");
			QueryResponse response = client.query("registry", query);
			
			SolrDocumentList documents = response.getResults();
			return (documents.getNumFound() != 0); 
		}
		catch (Exception e)
		{
			throw new CatalogException("Error while trying to find blob " + lidvid 
					+ ": " + e.getMessage());
		}
	}

	/**
	 * Ingests the product into the registry.
	 *
	 * @param searchUrl The URL to the Search Service.
	 * @param prodFile  The PDS4 product file.
	 * @param met       The metadata to register.
	 *
	 * @return The URL of the registered product.
	 * @throws IngestException If an error occurred while ingesting the product.
	 */
	public String ingest(URL searchUrl, File prodFile, Metadata met) throws IngestException 
	{
		String lid = met.getMetadata(Constants.LOGICAL_ID);
		String vid = met.getMetadata(Constants.PRODUCT_VERSION);
		String lidvid = lid + "::" + vid;

		try 
		{
			if(!hasProduct(searchUrl, lid, vid)) 
			{
				// Read the file content in memory
				byte[] fileContent = Files.readAllBytes(prodFile.toPath());				
				// Calculate MD5 hash
				byte[] md5hash = MessageDigest.getInstance("MD5").digest(fileContent);
				String strMd5 = Base64.getEncoder().encodeToString(md5hash);				
				// Base64 encode file content to store in Solr binary field
				String strFileContent = Base64.getEncoder().encodeToString(fileContent);
				
				// Create Solr document
				final SolrInputDocument doc = new SolrInputDocument();
				doc.addField("lid", lid);
				doc.addField("vid", vid);		
				doc.addField("lidvid", lidvid);
				doc.addField("name", prodFile.getName());
				doc.addField("md5", strMd5);
				doc.addField("content", strFileContent);
				doc.addField("package_id", TransactionManager.getInstance().getTransactionId());
				
				// Save the document
				SolrClient client = getClient(searchUrl);
				client.add("registry", doc);
				client.commit("registry");
				
				log.log(new ToolsLogRecord(ToolsLevel.SUCCESS, "Successfully registered product: " + lidvid, prodFile));
				++HarvestSolrStats.numProductsRegistered;
		
				try 
				{
					postXPaths(searchUrl, prodFile, met);
					++HarvestSolrStats.numXPathDocsRegistered;
				} 
				catch (Exception e) 
				{
					log.log(new ToolsLogRecord(ToolsLevel.INFO,
							"Error posting to xpath Solr Collection endpoint: " + e.getMessage()));
					++HarvestSolrStats.numXPathDocsNotRegistered;
				}
				
				return lidvid;
			} 
			else 
			{
				++HarvestSolrStats.numProductsNotRegistered;
				String message = "Product already exists: " + lidvid;
				log.log(new ToolsLogRecord(ToolsLevel.WARNING, message, prodFile));
				throw new IngestException(message);
			}
		} 
		catch(CatalogException c)
		{
			++HarvestSolrStats.numProductsNotRegistered;
			log.log(new ToolsLogRecord(ToolsLevel.SEVERE, "Error while " 
			+ "checking for the existence of a registered product: " + c.getMessage(), prodFile));
			throw new IngestException(c.getMessage());
		} 
		catch(Exception ex)
		{
			throw new IngestException(ex);
		} 
	}

	
	private void postXPaths(URL searchUrl, File prodFile, Metadata met) throws IOException, SolrServerException {
		String endPoint = "/xpath/update/json/docs";
		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest(endPoint);
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(prodFile));
			
			// Convert XML to JSON
			JSONObject json = XML.toJSONObject(br);
			
			// Add extra fields
			String lidvid = met.getMetadata(Constants.LOGICAL_ID) + "::" + met.getMetadata(Constants.PRODUCT_VERSION);
			json.append("id", lidvid);
			json.append("package_id", TransactionManager.getInstance().getTransactionId());
			
			// Store JSON in Solr
			StringStream stringStream = new StringStream(json.toString(2), MediaType.APPLICATION_JSON);
			up.addContentStream(stringStream);
			up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
			NamedList<Object> list = getClient(searchUrl).request(up);
			
			log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
					"Successfully posted document of XPaths of entire label to the xpath Solr collection", prodFile));
		} finally {
			IOUtils.closeQuietly(br);
		}
	}

	/**
	 * Method not implemented at this time.
	 *
	 */
	public String ingest(URL fmUrl, File prodFile, MetExtractor extractor, File metConfFile) throws IngestException {
		// No need for this method at this time
		return null;
	}

	/**
	 * Method not implemented at this time.
	 *
	 */
	public void ingest(URL fmUrl, List<String> prodFiles, MetExtractor extractor, File metConfFile)
			throws IngestException {
		// No need for this method at this time
	}
}
