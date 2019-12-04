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

package gov.nasa.pds.harvest.search.doc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import gov.nasa.pds.search.core.exception.SearchCoreException;
import gov.nasa.pds.search.core.exception.SearchCoreFatalException;
import gov.nasa.pds.search.core.schema.CoreConfigReader;
import gov.nasa.pds.search.core.schema.Product;
import gov.nasa.pds.search.core.schema.Query;


public class SearchConfigManager
{
	private static Logger LOG = Logger.getLogger(SearchConfigManager.class.getName());
	
	private static SearchConfigManager instance = new SearchConfigManager();
	
	private Map<String, Product> objTypeMap;
	private Map<String, Product> dataClassMap;
	
	
	private SearchConfigManager()
	{
		objTypeMap = new HashMap<>();
		dataClassMap = new HashMap<>();
	}
	
	
	public static SearchConfigManager getInstance()
	{
		return instance;
	}
	
	
	public void loadConfigs(File configDirectory) throws SearchCoreFatalException, SearchCoreException
	{
		List<File> configFiles = getCoreConfigs(configDirectory);
		
		for(File config: configFiles) 
		{
			try 
			{
				Product product = CoreConfigReader.unmarshall(config);
				
				for(Query query: product.getSpecification().getQuery())
				{
					String path = query.getRegistryPath();
					String value = query.getValue();
					
					if("objectType".equals(path) || "product_class".equals(path))
					{
						if(objTypeMap.containsKey(path))
						{
							LOG.warning(config.getAbsolutePath() 
								+ ": Configuration for objectType / product_class '" + value + "' already loaded.");
						}
						
						objTypeMap.put(value, product);
					}
					else if("data_class".equals(path))
					{
						if(dataClassMap.containsKey(path))
						{
							LOG.warning(config.getAbsolutePath() 
								+ ": Configuration for data_class '" + value + "' already loaded.");
						}

						dataClassMap.put(value, product);
					}
				}
			}
			catch(Exception ex) 
			{
				throw new SearchCoreFatalException("Error: Problem parsing " + config + "\nError Message: "
						+ ex.getMessage() + "\nCause: " + ex.getCause().getMessage());
			}
		}
	}
	

	public Product findConfigByObjectType(String objectType)
	{
		if(objectType == null) return null;
		return objTypeMap.get(objectType);
	}

	public Product findConfigByProductClass(String productClass)
	{
		if(productClass == null) return null;
		return objTypeMap.get(productClass);
	}
	
	public Product findConfigByDataClass(String dataClass)	
	{
		if(dataClass == null) return null;
		return dataClassMap.get(dataClass);
	}

	
	private static List<File> getCoreConfigs(File configDir) throws SearchCoreException 
	{
		if(configDir.isDirectory()) 
		{
			return new ArrayList<File>(FileUtils.listFiles(configDir, new String[] { "xml" }, true));
		}
		else if (configDir.isFile()) 
		{
			return Arrays.asList(configDir);
		} 
		else 
		{
			throw new SearchCoreException(configDir.getAbsolutePath() + " does not exist.");
		}
	}

}
