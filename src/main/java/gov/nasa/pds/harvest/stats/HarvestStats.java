package gov.nasa.pds.harvest.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HarvestStats {
  public static int numGoodFiles = 0;

  public static int numBadFiles = 0;

  public static int numFilesSkipped = 0;

  public static int numProductsRegistered = 0;

  public static int numProductsNotRegistered = 0;

  public static int numAncillaryProductsRegistered = 0;

  public static int numAncillaryProductsNotRegistered = 0;

  public static int numAssociationsRegistered = 0;

  public static int numAssociationsNotRegistered = 0;

  public static int numErrors = 0;

  public static int numWarnings = 0;

  public static HashMap<String, List<File>> registeredProductTypes = new HashMap<String, List<File>>();

  public static void addProductType(String type, File file) {
    if (registeredProductTypes.containsKey(type)) {
      List<File> list = registeredProductTypes.get(type);
      list.add(file);
      registeredProductTypes.put(type, list);
    } else {
      List<File> list = new ArrayList<File>();
      list.add(file);
      registeredProductTypes.put(type, list);
    }
  }
}
