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
 * ClassificationNode instances are used to define tree structures where each
 * node in the tree is a ClassificationNode. Such ClassificationScheme trees are
 * constructed with ClassificationNode instances under a ClassificationScheme
 * instance, and are used to define Classification schemes or ontologies.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "classificationNode", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassificationNode extends RegistryObject {

  private static final long serialVersionUID = -1068149748986904370L;

  /**
   * Reference to the guid of the parent ClassificationNode or
   * ClassificationScheme
   */
  @XmlAttribute
  private String parent;

  /**
   * A code with in a standard coding scheme that should be unique in respect to
   * all siblings
   */
  @XmlAttribute
  private String code;

  /**
   * Represents the traversal from the root scheme down to the node.
   */
  @XmlAttribute
  private String path;

  public ClassificationNode() {
    this.setObjectType(ClassificationNode.class.getSimpleName());
  }
  
  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
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
    ClassificationNode other = (ClassificationNode) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (parent == null) {
      if (other.parent != null)
        return false;
    } else if (!parent.equals(other.parent))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    return true;
  }
}
