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

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * ExternalIdentifier instances provide the additional identifier information to
 * RegistryObject such as DUNS number, Social Security Number, or an alias name
 * of the organization. The attribute identificationScheme is used to reference
 * the identification scheme (e.g., DUNS, Social Security #), and the
 * attribute value contains the actual information (e.g., the DUNS number, the
 * social security number). Each RegistryObject MAY contain 0 or more
 * ExternalIdentifier instances.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "externalIdentifier", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalIdentifier extends RegistryObject {
  private static final long serialVersionUID = 8027900663919476053L;

  /**
   * Each ExternalIdentifier instance MUST have an identificationScheme
   * attribute that references a ClassificationScheme. This ClassificationScheme
   * defines the namespace within which an identifier is defined using the value
   * attribute for the RegistryObject referenced by the RegistryObject
   * attribute.
   */
  private String identificationScheme;

  /**
   * Each ExternalIdentifier instance MUST have a registryObject attribute that
   * references the parent RegistryObject for which this is an
   * ExternalIdentifier.
   */
  private String registryObject;

  /**
   * Each ExternalIdentifier instance MUST have a value attribute that provides
   * the identifier value for this ExternalIdentifier (e.g., the actual
   * social security number).
   */
  private String value;

  public ExternalIdentifier() {
    this.setObjectType(ExternalIdentifier.class.getSimpleName());
  }

  public String getIdentificationScheme() {
    return identificationScheme;
  }

  public void setIdentificationScheme(String identificationScheme) {
    this.identificationScheme = identificationScheme;
  }

  public String getRegistryObject() {
    return registryObject;
  }

  public void setRegistryObject(String registryObject) {
    this.registryObject = registryObject;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime
        * result
        + ((identificationScheme == null) ? 0 : identificationScheme.hashCode());
    result = prime * result
        + ((registryObject == null) ? 0 : registryObject.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    ExternalIdentifier other = (ExternalIdentifier) obj;
    if (identificationScheme == null) {
      if (other.identificationScheme != null)
        return false;
    } else if (!identificationScheme.equals(other.identificationScheme))
      return false;
    if (registryObject == null) {
      if (other.registryObject != null)
        return false;
    } else if (!registryObject.equals(other.registryObject))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
