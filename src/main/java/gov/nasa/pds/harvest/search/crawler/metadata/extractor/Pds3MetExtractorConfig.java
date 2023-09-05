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

import java.util.List;

import org.apache.oodt.cas.metadata.MetExtractorConfig;
import gov.nasa.pds.harvest.search.policy.ElementName;
import gov.nasa.pds.harvest.search.policy.LidContents;
import gov.nasa.pds.harvest.search.policy.Pds3ProductMetadata;
import gov.nasa.pds.harvest.search.policy.Slot;
import gov.nasa.pds.harvest.search.policy.TitleContents;

public class Pds3MetExtractorConfig implements MetExtractorConfig {
  private List<Slot> staticMetadata;
  private LidContents lidContents;
  private List<ElementName> ancillaryMetadata;
  private List<String> includePaths;
  private TitleContents titleContents;

  /**
   * Default contstructor.
   *
   * @param metadata A class that contains what metadata
   * to extract from a PDS3 product.
   *
   */
  public Pds3MetExtractorConfig(Pds3ProductMetadata metadata) {
    staticMetadata = metadata.getStaticMetadata().getSlot();
    lidContents = metadata.getLidContents();
    titleContents = metadata.getTitleContents();
    ancillaryMetadata = metadata.getAncillaryMetadata().getElementName();
    includePaths = metadata.getIncludePaths().getPath();
  }

  /**
   * Gets the static metadata.
   *
   * @return The list of static metadata.
   */
  public List<Slot> getStaticMetadata() {
    return staticMetadata;
  }

  /**
   * Gets the lid contents.
   *
   * @return The lid contents.
   */
  public LidContents getLidContents() {
    return lidContents;
  }

  /**
   * Gets the title contents.
   *
   * @return The title contents.
   */
  public TitleContents getTitleContents() {
    return titleContents;
  }

  /**
   * Gets the ancillary metadata.
   *
   * @return Ancillary metadata.
   */
  public List<ElementName> getAncillaryMetadata() {
    return ancillaryMetadata;
  }

  /**
   * Gets include paths.
   *
   * @return include paths.
   */
  public List<String> getIncludePaths() {
    return includePaths;
  }
}
