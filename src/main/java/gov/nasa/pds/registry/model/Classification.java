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
 * A Classification instance classifies a RegistryObject instance by referencing
 * a node defined within a particular ClassificationScheme. An internal
 * Classification will always reference the node directly, by its id, while
 * an external Classification will reference the node indirectly by specifying a
 * representation of its value that is unique within the external
 * classification scheme.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "classification", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class Classification extends RegistryObject {
  
  private static final long serialVersionUID = 5094131149592645626L;

  /**
   * Reference to the guid for the classification scheme
   */
  @XmlAttribute
  private String classificationScheme;

  /**
   * Reference to the guid for the classificaiton node
   */
  @XmlAttribute
  private String classificiationNode;

  /**
   * Reference to guid of registry object
   */
  @XmlAttribute
  private String classifiedObject;

  /**
   * Identification string used when referencing external taxonomies
   */
  @XmlAttribute
  private String nodeRepresentation;
  
  public Classification() {
    this.setObjectType(Classification.class.getSimpleName());
  }
  
    
  public String getClassificationScheme() {
    return classificationScheme;
  }

  public void setClassificationScheme(String classificationScheme) {
    this.classificationScheme = classificationScheme;
  }

  public String getClassificiationNode() {
    return classificiationNode;
  }

  public void setClassificiationNode(String classificiationNode) {
    this.classificiationNode = classificiationNode;
  }

  public String getClassifiedObject() {
    return classifiedObject;
  }

  public void setClassifiedObject(String classifiedObject) {
    this.classifiedObject = classifiedObject;
  }

  public String getNodeRepresentation() {
    return nodeRepresentation;
  }

  public void setNodeRepresentation(String nodeRepresentation) {
    this.nodeRepresentation = nodeRepresentation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime
        * result
        + ((classificationScheme == null) ? 0 : classificationScheme.hashCode());
    result = prime * result
        + ((classificiationNode == null) ? 0 : classificiationNode.hashCode());
    result = prime * result
        + ((classifiedObject == null) ? 0 : classifiedObject.hashCode());
    result = prime * result
        + ((nodeRepresentation == null) ? 0 : nodeRepresentation.hashCode());
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
    Classification other = (Classification) obj;
    if (classificationScheme == null) {
      if (other.classificationScheme != null)
        return false;
    } else if (!classificationScheme.equals(other.classificationScheme))
      return false;
    if (classificiationNode == null) {
      if (other.classificiationNode != null)
        return false;
    } else if (!classificiationNode.equals(other.classificiationNode))
      return false;
    if (classifiedObject == null) {
      if (other.classifiedObject != null)
        return false;
    } else if (!classifiedObject.equals(other.classifiedObject))
      return false;
    if (nodeRepresentation == null) {
      if (other.nodeRepresentation != null)
        return false;
    } else if (!nodeRepresentation.equals(other.nodeRepresentation))
      return false;
    return true;
  }
}
