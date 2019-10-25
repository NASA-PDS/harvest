// Copyright 2006-2016, by the California Institute of Technology.
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
package gov.nasa.pds.harvest.search.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.pds.harvest.search.constants.Constants;
import gov.nasa.pds.harvest.search.stats.HarvestSolrStats;
import gov.nasa.pds.harvest.search.util.DocWriter;
import gov.nasa.pds.harvest.search.util.TransactionManager;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.Slot;
import gov.nasa.pds.registry.model.wrapper.ExtendedExtrinsicObject;
import gov.nasa.pds.search.core.exception.SearchCoreException;
import gov.nasa.pds.search.core.exception.SearchCoreFatalException;
import gov.nasa.pds.search.core.registry.ProductClassException;
import gov.nasa.pds.search.core.schema.Field;
import gov.nasa.pds.search.core.schema.OutputString;
import gov.nasa.pds.search.core.schema.OutputStringFormat;
import gov.nasa.pds.search.core.schema.Product;
import gov.nasa.pds.search.core.util.Debugger;


/**
 * Class that generates the Search document files.
 * 
 * @author mcayanan
 *
 */
public class SearchDocGenerator {
	private static final int SOLR_DOC_THRESHOLD = 1000;

	private File outputDirectory;

	private DocWriter writer = null;
	private HashMap<String, JsonElement> resources;
	private File resourceFile;  
  
	
	public SearchDocGenerator(File configDirectory, File outputDirectory, File resource)
			throws SearchCoreException, SearchCoreFatalException 
	{
		SearchConfigManager.getInstance().loadConfigs(configDirectory);
		
		this.outputDirectory = outputDirectory;
		this.resources = new HashMap<String, JsonElement>();
		this.resourceFile = resource;
		
		try {
			setResources(this.resourceFile);
		} catch (Exception e) {
			throw new SearchCoreFatalException("Error while parsing registered " + "resources: " + e.getMessage());
		}
	}

	
	private void setResources(File resource) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Gson gson = new Gson();
		JsonObject json = gson.fromJson(new FileReader(resource), JsonObject.class);
		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			this.resources.put(entry.getKey(), entry.getValue());
		}
	}
  
  /**
   * Generate the Solr document file for the given extrinsic object.
   * 
   * @param extrinsic Extrinsic object.
   * @param metadata Metadata associated with the given extrinsic object.
   * @param obj SearchDocState object.
   * 
   * @throws Exception If an error occurred while generating the document file.
   */
	public void generate(ExtrinsicObject extrinsic, Metadata metadata, SearchDocState obj) throws Exception 
	{
		// Extract Data Class
		String dataClass = null;		
		Slot slot = extrinsic.getSlot("data_class");
		if(slot != null)
		{
			List<String> values = slot.getValues();
			if(values != null && !values.isEmpty())
			{
				dataClass = values.get(0);
			}
		}
		
		// Object Type
		String objectType = extrinsic.getObjectType(); 
		
		// Find a configuration for this ExtrinsicObject / Metadata 
		SearchConfigManager mgr = SearchConfigManager.getInstance();
		Product config = mgr.findConfigByDataClass(dataClass);
		if(config == null)
		{
			config = mgr.findConfigByObjectType(objectType);
		}
		if(config == null) 
		{
			throw new Exception("Could not find a configuration file for " 
				+ "objectType '" + objectType + "' or data_class '" + dataClass + "'");
		}
		
		// Generate Solr XML document for this ExtrinsicObject / Metadata
		try 
		{
			Map<String, String> typeMap = new HashMap<String, String>();
			typeMap = setFieldTypes(config);

			// Create output directory
			createOutputDirectory();

			ExtendedExtrinsicObject extendedExtrinsic = new ExtendedExtrinsicObject(extrinsic);
			Map<String, List<String>> fieldMap = new HashMap<String, List<String>>();
			fieldMap.putAll(setFieldValues(extendedExtrinsic, config, metadata));
			
			// Add package ID
			HarvestSolrStats.packageId = TransactionManager.getInstance().getTransactionId();
			typeMap.put("package_id", "string");
			fieldMap.put("package_id", Arrays.asList(TransactionManager.getInstance().getTransactionId()));

			// Increment our product counter
			obj.incrementCounter();

			// Get the file number based on the THRESHOLD
			int outSeqNum = getOutputSeqNumber(obj.getCounter());

			writer = new DocWriter(fieldMap, this.outputDirectory, outSeqNum, 
					config.getSpecification().getTitle(), typeMap);

			// Write the file
			writer.write();

			++HarvestSolrStats.numDocumentsCreated;
			HarvestSolrStats.addProductType(extrinsic.getObjectType());
		} 
		catch(Exception ex) 
		{
			++HarvestSolrStats.numDocumentsNotCreated;
			throw ex;
		}
	}

	
  /**
   * Get all of the attributes and their values and place them into a HashMap,
   * valArray. The HashMap is made of of attrName->value pairs. The value in
   * the pair depends upon the current attribute's index, where it is either
   * the value in attrVals or a value queried from the database.
   * 
   * @see gov.nasa.pds.search.core.extractor.registry.MappingTypes
   * 
   * @param ExtrinsicObject object to be used.
   * @param config The configuration file.
   * @param metadata metadata associated with the given extrinsic object.
   * @throws ProductClassException  any errors throughout the querying of registry and
   *                  managing the data
   */
  private Map<String, List<String>> setFieldValues(
      ExtendedExtrinsicObject searchExtrinsic, Product config, Metadata metadata)
      throws ProductClassException {
    try {
      Map<String, List<String>> fieldMap = new HashMap<String, List<String>>();
      
      /* Initialize local variables */
      List<String> valueList = new ArrayList<String>();
      
      Object value;
      
      // Loop through class results beginning from top
      for (Field field : config.getIndexFields().getField()) {
        //TODO Functionality to use suffixes for field names commented out below
        String fieldName = field.getName(); //+ SolrSchemaField.getSuffix(field.getType());
        valueList = new ArrayList<String>();
        
        // Handle registry path
        if (!field.getRegistryPath().isEmpty()) {
          valueList = getSlotValuesFromPathList(field.getRegistryPath(), searchExtrinsic, metadata);
        } 
        
        if (valueList.isEmpty() && field.getOutputString() != null) { // Handle outputString
          value = field.getOutputString();
          valueList.add(checkForSubstring((OutputString)value, searchExtrinsic, metadata));
        }
        
        if (valueList.isEmpty() && field.getDefault() != null) {
          valueList.add(field.getDefault());
        }
        
        fieldMap.put(fieldName, valueList);
      }
      
      return fieldMap;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ProductClassException("Exception "
          + ex.getClass().getName() + ex.getMessage());
    }
  }
  
  /**
   * Extract the attribute/slot/association from the String
   * specified and query the Registry for the value to replace
   * it wit.
   * 
   * @param outputString input string from config
   * @param extObject extrinsic object.
   * @param metadata  metadata associated with the given extrinsic object.
   * @return the string with the embedded attribute/slot/association
   *         to be queried, replaced with the value from the Registry
   * @throws Exception
   */
  protected String checkForSubstring(OutputString outputString, 
      ExtendedExtrinsicObject extObject, Metadata metadata)
      throws Exception {
    
    String str = outputString.getValue();
    
    int start, end; 
    String key, value = "";

    List<String> valueList;
    while (str.contains("{")) {
      start = str.indexOf("{");
      end = str.indexOf("}", start);
      key = str.substring(start + 1, end);

      valueList = getSlotValuesFromPathList(Arrays.asList(key), extObject, metadata);     
      if (valueList != null && !valueList.isEmpty()) {
        if (outputString.getFormat().equals(OutputStringFormat.URL)) {
          value = URLEncoder.encode(valueList.get(0), "UTF-8");
        } else if (outputString.getFormat().equals(OutputStringFormat.TEXT)) {
          value = valueList.get(0);
        }
        str = str.replace("{" + key + "}", value);
          
      } else {
        str = str.replace("{" + key + "}", "");
      }
    }
    return str;
  }
  
  /**
   * Figures out if the registry paths are an association (dot-connected string) or
   * just a slot. If its an association, it starts traversing the path to get the values.
   * If its a slot, it returns the value list. The list of paths allow for multiple different
   * paths and are thought of as an OR.
   * 
   * @param registryPath
   * @param searchExtrinsic
   * @return
   * @throws Exception
   */
  private List<String> getSlotValuesFromPathList(List<String> registryPathList, 
      ExtendedExtrinsicObject searchExtrinsic, Metadata metadata)
          throws Exception {
    String[] pathArray;
    List<String> valueList = new ArrayList<String>();
    
    for (String registryPath : registryPathList) {
      pathArray = registryPath.split("\\.");
      if (pathArray.length > 1) {
        Debugger.debug("Traversing registry path - " + searchExtrinsic.getLid()
            + " - " + registryPath);        
        valueList.addAll(traverseRegistryPath(Arrays.asList(pathArray), 
            Arrays.asList(searchExtrinsic), metadata));
      } else {  // Field is a slot
        Debugger.debug("Getting slot values - " + searchExtrinsic.getLid()
            + " - " + registryPath);
        valueList.addAll(getValidSlotValues(searchExtrinsic, registryPath));
      }
    }
    return valueList;
  }
  
  /**
   * Search Service requires that when an association reference slot is queried from the index,
   * it returns a lidvid, if available. In the case where a reference value is a lid instead of
   * a lidvid, we will query the registry and attempt to build a lidvid from the most recent
   * version of the product.
   * 
   * If the slot in question is not an association, then it is just passed along like a hot potato.
   * 
   * @param searchExt
   * @param slotName
   * @return
   * @throws Exception
   */
  private List<String> getValidSlotValues(ExtendedExtrinsicObject searchExt, 
      String slotName) throws Exception {
    List<String> slotValues = new ArrayList<String>();
    if (searchExt.getSlotValues(slotName) != null) {
      slotValues.addAll(searchExt.getSlotValues(slotName));
      if (searchExt.slotIsAssociationReference(slotName)) {   
        // If slot is an association reference
        if (!searchExt.hasValidAssociationValues()) {   
          // If associations have values not are not lidvids
          // We will have to make the lidvids for them
          Debugger.debug("-- INVALID ASSOCIATION VALUE FOUND for " + searchExt.getLid() + " - " + slotName);
          List<String> newSlotValues = new ArrayList<String>();
          ExtendedExtrinsicObject assocSearchExt;
          for(String lid : slotValues) {
            //TODO: Need to handle this
            ExtrinsicObject e = Constants.collectionMap.get(lid);
            if (e != null) {
              assocSearchExt = new ExtendedExtrinsicObject(e);
            } else {
              assocSearchExt = null;
            }
            if (assocSearchExt != null) { // if association is found, add the lidvid to slot values
              //Debugger.debug("New slot value: " + assocSearchExt.getLidvid());
              newSlotValues.add(assocSearchExt.getLidvid());
            } else {
              //Debugger.debug("Association not found for new slot value, adding lid instead : " + lid);
              newSlotValues.add(lid);
            }
          }
          return newSlotValues;
        }
      }
    }
    
    return slotValues;
  }
  
  
  /**
   * Traverses down a registry path tree by looping through the dot-connected path.
   * Uses a fun little recursion to make it happen.
   * 
   * @param pathList
   * @param searchExtrinsicList
   * @return
   * @throws Exception
   */
  private List<String> traverseRegistryPath(List<String> pathList, 
      List<ExtendedExtrinsicObject> searchExtrinsicList, Metadata metadata)
          throws Exception {
    ArrayList<String> newPathList = null;
    if (pathList.size() > 1 && !searchExtrinsicList.isEmpty()) {
      newPathList = new ArrayList<String>();
      newPathList.addAll(pathList.subList(1, pathList.size()));
      for (ExtendedExtrinsicObject searchExtrinsic : searchExtrinsicList) {
        List<ExtendedExtrinsicObject> extendedExtrinsics = 
            new ArrayList<ExtendedExtrinsicObject>();
        List<ExtrinsicObject> extrinsics = new ArrayList<ExtrinsicObject>();
        if ("file_ref".equalsIgnoreCase(pathList.get(0))) {
          if (metadata.containsKey("file_ref")) {
            extrinsics.addAll(metadata.getAllMetadata("file_ref"));
          }
        } else if ("collection_ref".equalsIgnoreCase(pathList.get(0))) {
          List<String> refs = searchExtrinsic.getSlotValues("collection_ref");
          if (refs != null) {
            for (String ref : refs) {
              if (Constants.collectionMap.containsKey(ref)) {
                extrinsics.add(Constants.collectionMap.get(ref));
              }
            }
          }
        } else if ("resource_ref".equalsIgnoreCase(pathList.get(0))) {
          List<String> refs = searchExtrinsic.getSlotValues("resource_ref");
          if (refs != null) {
            for (String ref : refs) {
              if (resources.containsKey(ref)) {
                JsonObject members = resources.get(ref).getAsJsonObject();
                ExtrinsicObject resource = new ExtrinsicObject();
                resource.setLid(ref);
                for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
                  List<String> values = new ArrayList<String>();
                  values.add(entry.getValue().getAsString());
                  resource.addSlot(new Slot(entry.getKey(), values));
                }
                extrinsics.add(resource);
              }
            }
          }
        }
        for (ExtrinsicObject extrinsic : extrinsics) {
          extendedExtrinsics.add(new ExtendedExtrinsicObject(extrinsic));
        }
        if (!extendedExtrinsics.isEmpty()) {
          //Do we need to add this?
          //extendedExtrinsics.add(searchExtrinsic);
          return traverseRegistryPath(newPathList, extendedExtrinsics, 
              metadata);
        }
      }
    } else if (pathList.size() == 1 && !searchExtrinsicList.isEmpty()) {  
      // Let's get some slot values
      List<String> slotValueList = new ArrayList<String>();
      for (ExtendedExtrinsicObject searchExtrinsic : searchExtrinsicList) {
        slotValueList.addAll(getValidSlotValues(searchExtrinsic, pathList.get(0)));
      }
      return slotValueList;
    }
    return new ArrayList<String>();
  }
  
  private Map<String, String> setFieldTypes(Product config) 
      throws ProductClassException {
    try {
      Map<String, String> typeMap = new HashMap<String, String>();

      /* Initialize local variables */
      List<String> valueList = new ArrayList<String>();

      Object value;

      // Loop through class results beginning from top
      for (Field field : config.getIndexFields().getField()) {
        //TODO Functionality to use suffixes for field names commented out below
        String fieldName = field.getName(); //+ SolrSchemaField.getSuffix(field.getType());
        String fieldType = field.getType().value();

        typeMap.put(fieldName, fieldType);
      }

      return typeMap;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ProductClassException("Exception "
          + ex.getClass().getName() + ex.getMessage());
    }
  }
  
  /**
   * Create output directory for XML files
   * @return
   * @throws SearchCoreFatalException
   */
  private void createOutputDirectory() 
      throws SearchCoreFatalException {
    try {
      FileUtils.forceMkdir(this.outputDirectory);
    } catch (IOException e) {
      throw new SearchCoreFatalException("Could not create directory: "
        + this.outputDirectory);
    }
  }
  
  /**
   * Check if there are files in the output directory. If so, assuming the files
   * are from a previous run of the Search Core, add the count to the sequence
   * number constant in order to add to the files instead of overwriting them.
   * 
   * @param   outDir
   * @return  start number for the suffix for the index docs
   */
  private int getOutputSeqNumber(int counter) {
    return counter / SOLR_DOC_THRESHOLD;
  }

}
