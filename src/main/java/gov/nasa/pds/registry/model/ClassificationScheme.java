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
 * A ClassificationScheme instance describes a taxonomy. The taxonomy hierarchy
 * may be defined internally to the registry by instances of ClassificationNode,
 * or it may be defined externally to the Registry, in which case the structure
 * and values of the taxonomy elements are not known to the Registry.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "classificationScheme", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassificationScheme extends RegistryObject {
  
  private static final long serialVersionUID = 5010025503737861598L;

  /**
   * When submitting a ClassificationScheme instance the submitter MUST declare
   * whether the ClassificationScheme instance represents an internal or an
   * external taxonomy. This allows the registry to validate the subsequent
   * submissions of ClassificationNode and Classification instances in order to
   * maintain the type of ClassificationScheme consistent throughout its
   * lifecycle.
   */
  @XmlAttribute
  private Boolean isInternal;

  /**
   * UniqueCode: This value indicates that each node of the taxonomy has a
   * unique code assigned to it. 
   * 
   * EmbeddedPath: This value indicates that the
   * unique code assigned to each node of the taxonomy also encodes its path.
   * This is the case in the NAICS taxonomy. 
   * 
   * NonUniqueCode: In some cases nodes
   * are not unique, and it is necessary to use the full path (from
   * ClassificationScheme to the node of interest) in order to identify the
   * node. For example, in a geography taxonomy Moscow could be under both
   * Russia and the USA, where there are five cities of that name in different
   * states.
   */
  @XmlAttribute
  private NodeType nodeType;

  public ClassificationScheme() {
    this.setObjectType(ClassificationScheme.class.getSimpleName());
  }

  public Boolean getIsInternal() {
    return isInternal;
  }

  public void setIsInternal(Boolean isInternal) {
    this.isInternal = isInternal;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((isInternal == null) ? 0 : isInternal.hashCode());
    result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
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
    ClassificationScheme other = (ClassificationScheme) obj;
    if (isInternal == null) {
      if (other.isInternal != null)
        return false;
    } else if (!isInternal.equals(other.isInternal))
      return false;
    if (nodeType == null) {
      if (other.nodeType != null)
        return false;
    } else if (!nodeType.equals(other.nodeType))
      return false;
    return true;
  }
}
