package gov.nasa.pds.harvest.stats;

import java.io.File;
import java.math.BigInteger;
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

  public static int numGeneratedChecksumsSameInManifest = 0;

  public static int numGeneratedChecksumsDiffInManifest = 0;

  public static int numGeneratedChecksumsNotCheckedInManifest = 0;

  public static int numGeneratedChecksumsSameInLabel = 0;

  public static int numGeneratedChecksumsDiffInLabel = 0;

  public static int numGeneratedChecksumsNotCheckedInLabel = 0;

  public static int numManifestChecksumsSameInLabel = 0;

  public static int numManifestChecksumsDiffInLabel = 0;

  public static int numManifestChecksumsNotCheckedInLabel = 0;


  public static HashMap<String, BigInteger> registeredProductTypes = new HashMap<String, BigInteger>();

  public static void addProductType(String type) {
    if (registeredProductTypes.containsKey(type)) {
      BigInteger count = registeredProductTypes.get(type);
      count = count.add(BigInteger.ONE);
      registeredProductTypes.put(type, count);
    } else {
      registeredProductTypes.put(type, BigInteger.ONE);
    }
  }
}
