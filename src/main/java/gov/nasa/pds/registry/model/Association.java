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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class allows one to relate a source and target registry object along a
 * named relationship. The source and target can be identified by either the
 * guid or the pairing of a lid vid. The home indicates where the registered
 * originated.
 * 
 * @author pramirez
 * 
 */

@Entity
@XmlRootElement(name = "association", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class Association extends RegistryObject {

	private static final long serialVersionUID = -7276791609858383804L;

	/**
	 * Unique identifier of the source registry object.
	 */
	@XmlAttribute
	private String sourceObject;

	/**
	 * Unique identifier for the target registry object.
	 */
	@XmlAttribute
	private String targetObject;
	
	/**
	 * Named relation between source and target registry object.
	 */
	@XmlAttribute
	private String associationType;

	public Association() {
		this.setObjectType(Association.class.getSimpleName());
		this.setVersionName(null);
	}

	/**
	 * @return the type of association that exists between the source and target
	 *         artifact
	 */
	public String getAssociationType() {
		return associationType;
	}

	/**
	 * @param associationType
	 *            the type of association between the source and target
	 */
	public void setAssociationType(String associationType) {
		this.associationType = associationType;
	}

	public String getSourceObject() {
		return sourceObject;
	}

	public void setSourceObject(String sourceObject) {
		this.sourceObject = sourceObject;
	}
	
	public String getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(String targetObject) {
		this.targetObject = targetObject;
	}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((associationType == null) ? 0 : associationType.hashCode());
    result = prime * result
        + ((sourceObject == null) ? 0 : sourceObject.hashCode());
    result = prime * result
        + ((targetObject == null) ? 0 : targetObject.hashCode());
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
    Association other = (Association) obj;
    if (associationType == null) {
      if (other.associationType != null)
        return false;
    } else if (!associationType.equals(other.associationType))
      return false;
    if (sourceObject == null) {
      if (other.sourceObject != null)
        return false;
    } else if (!sourceObject.equals(other.sourceObject))
      return false;
    if (targetObject == null) {
      if (other.targetObject != null)
        return false;
    } else if (!targetObject.equals(other.targetObject))
      return false;
    return true;
  }

}
