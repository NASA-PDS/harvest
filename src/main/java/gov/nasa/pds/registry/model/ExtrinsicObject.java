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

@Entity
@XmlRootElement(name = "extrinsicObject", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtrinsicObject extends RegistryObject {

  private static final long serialVersionUID = 4629220512391515679L;

  /**
   * Each ExtrinsicObject instance MAY have a contentVersion attribute. The value of the
   * contentVersion attribute. The contentVersion attribute provides information
   * about the specific version of the RepositoryItem associated with a ExtrinsicObject.
   * The contentVersion attribute is set by the registry.
   */
  @XmlAttribute
  private String contentVersion;

  /**
   * Each ExtrinsicObject instance MAY have a mimeType attribute defined. The mimeType
   * provides information on the type of repository item catalogued by the
   * Product instance. The value of this attribute SHOULD be a registered MIME
   * media type at http://www.iana.org/assignments/media-types.
   */
  @XmlAttribute
  private String mimeType;

  public ExtrinsicObject() {
    this.setObjectType(ExtrinsicObject.class.getSimpleName());
  }

  public String getContentVersion() {
    return contentVersion;
  }

  public void setContentVersion(String contentVersion) {
    this.contentVersion = contentVersion;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((contentVersion == null) ? 0 : contentVersion.hashCode());
    result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
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
    ExtrinsicObject other = (ExtrinsicObject) obj;
    if (contentVersion == null) {
      if (other.contentVersion != null)
        return false;
    } else if (!contentVersion.equals(other.contentVersion))
      return false;
    if (mimeType == null) {
      if (other.mimeType != null)
        return false;
    } else if (!mimeType.equals(other.mimeType))
      return false;
    return true;
  }

}
