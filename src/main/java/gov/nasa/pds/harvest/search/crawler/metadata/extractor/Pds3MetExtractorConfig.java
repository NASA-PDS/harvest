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
package gov.nasa.pds.harvest.search.crawler.metadata.extractor;

import java.util.List;

import gov.nasa.jpl.oodt.cas.metadata.MetExtractorConfig;
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
