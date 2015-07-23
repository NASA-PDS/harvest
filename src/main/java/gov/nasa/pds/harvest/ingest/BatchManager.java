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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.stats.HarvestStats;
import gov.nasa.pds.registry.client.RegistryClient;
import gov.nasa.pds.registry.exception.RegistryServiceException;
import gov.nasa.pds.registry.model.Association;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.RegistryObject;
import gov.nasa.pds.registry.model.RegistryObjectList;

/**
 * Class that manages the batch ingestion of products into the Registry.
 *
 * @author mcayanan
 */
public class BatchManager {
  /** Logger object. */
  private static Logger log = Logger.getLogger(BatchManager.class.getName());

  /** Registry client. */
  private RegistryClient client;

  /** RegistryObjects to be versioned. */
  private LinkedHashMap<File, List<RegistryObject>> versionObjects;

  /** RegistryObjects to be published. */
  private LinkedHashMap<File, List<RegistryObject>> publishObjects;

  /**
   * Constructor.
   *
   * @param client A RegistryClient object.
   */
  public BatchManager(RegistryClient client) {
    this.client = client;
    versionObjects = new LinkedHashMap<File, List<RegistryObject>>();
    publishObjects = new LinkedHashMap<File, List<RegistryObject>>();
  }

  /**
   * Caches the given RegistryObject for future registration.
   *
   * @param object The RegistryObject to ingest.
   * @param product The product file associated with the given RegistryObject.
   * @param version Flag to indicate whether to version the object.
   *
   */
  public void cache(RegistryObject object, File product, boolean version) {
    if (version) {
      List<RegistryObject> objects = versionObjects.get(product);
      if (objects == null) {
        objects = new ArrayList<RegistryObject>();
        objects.add(object);
        versionObjects.put(product, objects);
      } else {
        objects.add(object);
        versionObjects.put(product, objects);
      }
    } else {
      List<RegistryObject> objects = publishObjects.get(product);
      if (objects == null) {
        objects = new ArrayList<RegistryObject>();
        objects.add(object);
        publishObjects.put(product, objects);
      } else {
        objects.add(object);
        publishObjects.put(product, objects);
      }
    }
  }

  /**
   * @return The size of the cache.
   */
  public int getCacheSize() {
    return versionObjects.size() + publishObjects.size();
  }

  public RegistryClient getRegistryClient() {
    return this.client;
  }

  /**
   * Ingests all products that have been cached.
   *
   */
  public void ingest() {
    ingest(publishObjects, false);
    ingest(versionObjects, true);
    publishObjects.clear();
    versionObjects.clear();
  }

  /**
   * Ingests the given registry objects.
   *
   * @param objects The registry objects to register.
   * @param version Set to true to version the objects. False otherwise.
   */
  private void ingest(LinkedHashMap<File, List<RegistryObject>> objects,
      boolean version) {
    List<RegistryObject> registryObjects = new ArrayList<RegistryObject>();
    for (Map.Entry<File, List<RegistryObject>> entry
        : objects.entrySet()) {
      registryObjects.addAll(entry.getValue());
    }
    if (registryObjects.size() == 1) {
      RegistryObject registryObject = registryObjects.get(0);
      File product = objects.keySet().iterator().next();
      try {
        if (version) {
          client.versionObject(registryObjects.get(0));
        } else {
          client.publishObject(registryObjects.get(0));
        }
        logResults(objects, false);
      } catch (RegistryServiceException e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, e.getMessage()));
        if (registryObject instanceof Association) {
          String targetRef = ((Association) registryObject).getTargetObject();
          log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
              "Association to '" + targetRef + "' was not registered",
               product));
          ++HarvestStats.numAssociationsNotRegistered;
        } else {
          String lidvid = registryObject.getLid() + "::"
              + registryObject.getSlot(Constants.PRODUCT_VERSION)
              .getValues().get(0);
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
              lidvid + " was not registered.", product));
          if (Constants.FILE_OBJECT_PRODUCT_TYPE.equals(
              registryObject.getObjectType())) {
            ++HarvestStats.numAncillaryProductsNotRegistered;
          } else {
            ++HarvestStats.numProductsNotRegistered;
          }
        }
      }
    } else if (!registryObjects.isEmpty()) {
      RegistryObjectList list = new RegistryObjectList();
      list.setObjects(registryObjects);
      try {
        if (version) {
          client.versionObjects(list, true);
        } else {
          client.publishObjects(list);
        }
        logResults(objects, false);
      } catch (RegistryServiceException e) {
        log.log(new ToolsLogRecord(ToolsLevel.SEVERE, e.getMessage()));
        logResults(objects, true);
      }
    }
  }

  /**
   * Logs the given objects.
   *
   * @param registryObjects The registry objects.
   * @param verify Set to 'true' to verify that the given object does in
   * fact exist in the registry.
   */
  private void logResults(
      LinkedHashMap<File, List<RegistryObject>> registryObjects,
      boolean verify) {
    for (Map.Entry<File, List<RegistryObject>> entry
        : registryObjects.entrySet()) {
      for (RegistryObject object : entry.getValue()) {
        try {
          if (object instanceof Association) {
            Association association = (Association) object;
            if (verify) {
              client.getObject(association.getGuid(), Association.class);
            }
            String targetRef = association.getTargetObject();
            log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
                "Successfully registered association to '" + targetRef + "'",
                 entry.getKey()));
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
                "Association has the following GUID: " + object.getGuid(),
                entry.getKey()));
            ++HarvestStats.numAssociationsRegistered;
          } else {
            if (verify) {
              client.getObject(object.getGuid(), ExtrinsicObject.class);
            }
            String lidvid = object.getLid() + "::"
                + object.getSlot(Constants.PRODUCT_VERSION)
                .getValues().get(0);
            log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
                "Successfully registered product: " + lidvid,
                 entry.getKey()));
            log.log(new ToolsLogRecord(ToolsLevel.INFO,
                "Product has the following GUID: " + object.getGuid(),
                entry.getKey()));
            if (object instanceof ExtrinsicObject) {
              if (Constants.FILE_OBJECT_PRODUCT_TYPE.equals(
                  object.getObjectType())) {
                ++HarvestStats.numAncillaryProductsRegistered;
                HarvestStats.addProductType(
                    Constants.FILE_OBJECT_PRODUCT_TYPE);
              } else {
                ++HarvestStats.numProductsRegistered;
                HarvestStats.addProductType(object.getObjectType());
              }
            }
          }
        } catch (RegistryServiceException r) {
          if (object instanceof Association) {
            String targetRef = ((Association) object).getTargetObject();
            log.log(new ToolsLogRecord(ToolsLevel.SUCCESS,
                "Association to '" + targetRef + "' was not registered",
                 entry.getKey()));
            ++HarvestStats.numAssociationsNotRegistered;
          } else {
            String lidvid = object.getLid() + "::"
                + object.getSlot(Constants.PRODUCT_VERSION)
                .getValues().get(0);
            log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
                lidvid + " was not registered.", entry.getKey()));
            if (Constants.FILE_OBJECT_PRODUCT_TYPE.equals(
                object.getObjectType())) {
              ++HarvestStats.numAncillaryProductsNotRegistered;
            } else {
              ++HarvestStats.numProductsNotRegistered;
            }
          }
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE,
              r.getMessage(), entry.getKey()));
        }
      }
    }
  }
}
