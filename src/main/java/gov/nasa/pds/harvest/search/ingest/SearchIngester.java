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

import java.io.File;
import java.net.URL;
import java.util.List;

import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.IngestException;
import gov.nasa.jpl.oodt.cas.metadata.MetExtractor;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;

import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.search.registry.FileData;
import gov.nasa.pds.harvest.search.registry.FileDataLoader;
import gov.nasa.pds.harvest.search.registry.MetadataExtractor;
import gov.nasa.pds.harvest.search.registry.RegistryDAO;
import gov.nasa.pds.harvest.search.registry.RegistryMetadata;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;


/**
 * Class that supports ingestion of PDS4 product labels as a blob into the PDS
 * Search Service.
 *
 * @author mcayanan
 *
 */
public class SearchIngester implements Ingester 
{
	private static Logger log = Logger.getLogger(SearchIngester.class.getName());

	private MetadataExtractor metaExtractor;
	private RegistryDAO registryDAO;

	/**
	 * Default constructor.
	 */
	public SearchIngester() throws Exception
	{
	    metaExtractor = new MetadataExtractor();
	    registryDAO = new RegistryDAO();
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
	    String lidvid = lid + "::" + vid;
	    
	    try
	    {
    		return registryDAO.hasProduct(lid, vid);
	    }
	    catch(Exception ex)
	    {
            throw new CatalogException("Error while trying to find blob " + lidvid + ": " + ex.getMessage());
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
			    RegistryMetadata registryMeta = metaExtractor.extract(prodFile.getAbsolutePath());
			    
			    // Save product file
			    registryDAO.saveProduct(registryMeta, prodFile);
			    
				log.log(new ToolsLogRecord(ToolsLevel.SUCCESS, "Successfully registered product: " + lidvid, prodFile));
				++HarvestSolrStats.numProductsRegistered;
		
				// Save XPaths
				/*
				try
				{
					XPathDAO.postXPaths(prodFile, lid, vid);
		            log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
		                    "Successfully posted document of XPaths of entire label to the xpath Solr collection", prodFile));
					++HarvestSolrStats.numXPathDocsRegistered;
				} 
				catch (Exception e) 
				{
					log.log(new ToolsLogRecord(ToolsLevel.INFO,
							"Error posting to xpath Solr Collection endpoint: " + e.getMessage()));
					++HarvestSolrStats.numXPathDocsNotRegistered;
				}
				*/
				
				return lidvid;
			} 
			else 
			{
				++HarvestSolrStats.numProductsNotRegistered;
				String message = "Product already exists: " + lidvid;
				log.log(new ToolsLogRecord(ToolsLevel.WARNING, message, prodFile));
				return null;
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
		    ++HarvestSolrStats.numProductsNotRegistered;
		    log.log(new ToolsLogRecord(ToolsLevel.SEVERE, ex.getMessage(), prodFile));
		    throw new IngestException(ex);
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
