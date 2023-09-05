//	Copyright 2009-2010, by the California Institute of Technology.
//	ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
//	Any commercial use must be negotiated with the Office of Technology 
//	Transfer at the California Institute of Technology.
//	
//	This software is subject to U. S. export control laws and regulations 
//	(22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software 
//	is subject to U.S. export control laws and regulations, the recipient has 
//	the responsibility to obtain export licenses or other export authority as 
//	may be required before exporting such information to foreign countries or 
//	providing access to foreign nationals.
//	
//	$Id$
//

package gov.nasa.pds.registry.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The RegistryObject class extends the Identifiable class and serves as a
 * common super class for most classes in the information model.
 * 
 * @author pramirez
 * 
 */
@MappedSuperclass
@XmlRootElement(name = "registryObject", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "registryObjectType", namespace = "http://registry.pds.nasa.gov")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistryObject extends Identifiable {

  private static final long serialVersionUID = 1477919575493686135L;

  @OneToMany(cascade = CascadeType.ALL)
  @XmlElementRef(name="classification", namespace = "http://registry.pds.nasa.gov")
  @OrderBy
  private Set<Classification> classifications;
  
  @OneToMany(cascade = CascadeType.ALL)
  @XmlElementRef(name="externalIdentifier", namespace = "http://registry.pds.nasa.gov")
  @OrderBy
  private Set<ExternalIdentifier> externalIdentifiers;

  // Logical identifier supplied by submitter
  @XmlAttribute
  private String lid;

  // Name for display
  @XmlAttribute
  private String name;

  // Type of registry object which should map to policy about the slots
  @XmlAttribute
  private String objectType;

  // Submitted, Approved, Deprecated, etc.
  @XmlAttribute
  private ObjectStatus status;

  // Brief description of the entry
  @XmlAttribute
  private String description;

  // Version provided by registry
  @XmlAttribute
  private String versionName;

  /**
   * Meant for JAXB only
   */
  public RegistryObject() {
  }

  /**
   * Construct a registry object without a version
   * 
   * @param guid
   *          globally unique identifier
   * @param home
   *          registry URI
   * @param lid
   *          logical identifier
   * @param name
   * @param objectType
   */
  public RegistryObject(String guid, String home, String lid, String name,
      String objectType) {
    super(guid, home, new HashSet<Slot>());
    this.lid = lid;
    this.name = name;
    this.objectType = objectType;
  }

  public RegistryObject(RegistryObject object) {
    super(object.getGuid(), object.getHome(), object.getSlots());
    this.lid = object.getLid();
    this.name = object.getName();
    this.objectType = object.getObjectType();
    this.status = object.getStatus();
    this.description = object.getDescription();
    this.versionName = object.getVersionName();
  }

  /**
   * @return the lid
   */
  public String getLid() {
    return lid;
  }

  /**
   * @param lid
   *          the lid to set
   */
  public void setLid(String lid) {
    this.lid = lid;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the objectType
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * @param objectType
   *          the objectType to set
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  /**
   * @return the status
   */
  public ObjectStatus getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(ObjectStatus status) {
    this.status = status;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the internally tracked version
   */
  public String getVersionName() {
    return versionName;
  }

  /**
   * @param versionName
   *          the version to set for the registry tracked version
   */
  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public Set<Classification> getClassifications() {
    return classifications;
  }

  public void setClassifications(Set<Classification> classifications) {
    this.classifications = classifications;
  }

  public Set<ExternalIdentifier> getExternalIdentifiers() {
    return externalIdentifiers;
  }

  public void setExternalIdentifiers(Set<ExternalIdentifier> externalIdentifiers) {
    this.externalIdentifiers = externalIdentifiers;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((classifications == null) ? 0 : classifications.hashCode());
    result = prime * result
        + ((description == null) ? 0 : description.hashCode());
    result = prime * result
        + ((externalIdentifiers == null) ? 0 : externalIdentifiers.hashCode());
    result = prime * result + ((lid == null) ? 0 : lid.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result
        + ((objectType == null) ? 0 : objectType.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result
        + ((versionName == null) ? 0 : versionName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RegistryObject other = (RegistryObject) obj;
    if (classifications == null) {
      if (other.classifications != null)
        return false;
    } else if (!classifications.equals(other.classifications))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (externalIdentifiers == null) {
      if (other.externalIdentifiers != null)
        return false;
    } else if (!externalIdentifiers.equals(other.externalIdentifiers))
      return false;
    if (lid == null) {
      if (other.lid != null)
        return false;
    } else if (!lid.equals(other.lid))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (objectType == null) {
      if (other.objectType != null)
        return false;
    } else if (!objectType.equals(other.objectType))
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    if (versionName == null) {
      if (other.versionName != null)
        return false;
    } else if (!versionName.equals(other.versionName))
      return false;
    return true;
  }

}
