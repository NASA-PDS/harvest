// Copyright 2006-2013, by the California Institute of Technology.
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

package gov.nasa.pds.harvest.crawler.metadata;

/**
 * Class that holds Observing System Component metadata stored in a product
 * label.
 *
 * @author mcayanan
 *
 */
public class ObservingSystemComponent {
  /** The name. */
  private String name;

  /** The type. */
  private String type;

  /** The reference. */
  private String reference;

  /** A flag indicating if the component contains an internal reference. */
  private boolean hasReference;

  /**
   * Constructor.
   *
   * @param name The component name.
   * @param type The component type.
   */
  public ObservingSystemComponent(String name, String type) {
    this (name, type, null);
  }

  /**
   * Constructor.
   *
   * @param name The component name.
   * @param type The component type.
   * @param reference The component reference.
   */
  public ObservingSystemComponent(String name, String type, String reference) {
    this.name = name;
    this.type = type;
    if (reference != null) {
      this.reference = reference;
      hasReference = true;
    } else {
      this.reference = null;
      hasReference = false;
    }
  }

  /**
   * Get the name.
   *
   * @return The component name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the type.
   *
   * @return The component type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Get the reference.
   *
   * @return The component reference.
   */
  public String getReference() {
    return this.reference;
  }

  /**
   * Indicates whether the component has a reference.
   *
   * @return 'true' if yes, 'false' if no.
   */
  public boolean hasReference() {
    return this.hasReference;
  }
}
