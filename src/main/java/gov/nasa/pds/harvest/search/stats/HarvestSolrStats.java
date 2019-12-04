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

package gov.nasa.pds.harvest.search.stats;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HarvestSolrStats {
  public static int numGoodFiles = 0;

  public static int numBadFiles = 0;

  public static int numFilesSkipped = 0;
  
  public static int numProductsRegistered = 0;

  public static int numProductsNotRegistered = 0;

  public static int numDocumentsCreated = 0;

  public static int numDocumentsNotCreated = 0;

  public static int numXPathDocsRegistered = 0;

  public static int numXPathDocsNotRegistered = 0;
  
  public static int numAncillaryProductsRegistered = 0;

  public static int numAncillaryProductsNotRegistered = 0;

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
  
  public static String packageId = "N/A";

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
