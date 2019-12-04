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

package gov.nasa.pds.harvest.search.crawler.metadata.extractor;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpl.oodt.cas.metadata.MetExtractorConfig;
import gov.nasa.pds.harvest.search.policy.Pds4ProductMetadata;
import gov.nasa.pds.harvest.search.policy.ReferenceTypeMap;
import gov.nasa.pds.harvest.search.policy.References;
import gov.nasa.pds.harvest.search.policy.XPath;

/**
 * Configuration class for extracting metadata from
 * PDS4 data products.
 *
 * @author mcayanan
 *
 */
public class Pds4MetExtractorConfig implements MetExtractorConfig {
    /** Candidate products. */
    private List<Pds4ProductMetadata> pds4Candidates;

    private References references;
    /**
     * Default contstructor.
     *
     * @param candidates A class that contains what product types
     * to extract and which metadata fields to get from those
     * product types.
     *
     */
    public Pds4MetExtractorConfig(List<Pds4ProductMetadata> candidates, References references) {
      pds4Candidates = candidates;
      this.references = references;
    }

    /**
     * Gets XPath expressions for an object type.
     *
     * @param objectType The PDS object type.
     *
     * @return A list of XPath expressions based on the given object type.
     */
    public List<XPath> getMetXPaths(String objectType) {
        for (Pds4ProductMetadata p : pds4Candidates) {
            if (p.getObjectType().equalsIgnoreCase(objectType)) {
                return p.getXPath();
            }
        }
        return new ArrayList<XPath>();
    }

    /**
     * Determines whether an object type exists in the configuration class.
     *
     * @param objectType The object type to search.
     *
     * @return true if the supplied object type was found.
     */
    public boolean hasObjectType(String objectType) {
        for (Pds4ProductMetadata p : pds4Candidates) {
            if (p.getObjectType().equalsIgnoreCase(objectType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the mapped reference type of the given model value.
     *
     * @param modelValue The model value.
     *
     * @return The mapped reference type associated with the given model
     *  value. Returns 'null' if nothing was found.
     */
    public String getReferenceTypeMap(String modelValue) {
      for (ReferenceTypeMap refMap : references.getReferenceTypeMap()) {
        for (String value : refMap.getModelValue()) {
          if (value.trim().equals(modelValue)) {
            return refMap.getValue();
          }
        }
      }
      return null;
    }

    /**
     * Determines whether the config contains a reference type map.
     *
     * @return 'true' if yes, 'false' otherwise.
     */
    public boolean containsReferenceTypeMap() {
      if (!references.getReferenceTypeMap().isEmpty()) {
        return true;
      } else {
        return false;
      }
    }
}
