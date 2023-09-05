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

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A SpecificationLink provides the linkage between a ServiceBinding and one of
 * its technical specifications that describes how to use the service using the
 * ServiceBinding. For example, a ServiceBinding MAY have SpecificationLink
 * instances that describe how to access the service using a technical
 * specification such as a WSDL document, CORBA IDL document, or WADL document.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "specificationLink", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpecificationLink extends RegistryObject {

  private static final long serialVersionUID = 5133545037773476971L;

  /**
   * A SpecificationLink instance MUST have a serviceBinding attribute that
   * provides a reference to its parent ServiceBinding instances. Its value MUST
   * be the guid of the parent ServiceBinding object.
   */
  @XmlAttribute
  private String serviceBinding;

  /**
   * A SpecificationLink instance MUST have a specificationObject attribute that
   * provides a reference to a RegistryObject instance (via guid) that provides
   * a technical specification for the parent ServiceBinding. Typically, this is
   * an ExtrinsicObject instance representing the technical specification (e.g.,
   * a WSDL document). It may also be an ExternalLink object in case the
   * technical specification is a resource that is external to the registry.
   */
  @XmlAttribute
  private String specificationObject;

  /**
   * A SpecificationLink instance MAY have a usageDescription attribute that
   * provides a textual description of how to use the optional usageParameters
   * attribute described next. The usageDescription is of type
   * InternationalString, thus allowing the description to be in multiple
   * languages.
   */
  @Column(name = "use_desc", length = Integer.MAX_VALUE - 1)
  @Lob
  @XmlElement(name = "usageDescription", namespace = "http://registry.pds.nasa.gov")
  private String usageDescription;

  /**
   * A SpecificationLink instance MAY have a usageParameters attribute that
   * provides a Bag of Strings representing the instance specific parameters
   * needed to use the technical specification (e.g., a WSDL document) specified
   * by this SpecificationLink object
   */
  @ElementCollection
  @CollectionTable(name = "Usage_Parameters", joinColumns = @JoinColumn(name = "spec_id"))
  @Column(name = "param", length = Integer.MAX_VALUE - 1)
  @Lob
  @XmlElement(name = "usageParameter", namespace = "http://registry.pds.nasa.gov")
  private List<String> usageParameters;

  public SpecificationLink() {
    this.setObjectType(SpecificationLink.class.getSimpleName());
  }
  
  public String getServiceBinding() {
    return serviceBinding;
  }

  public void setServiceBinding(String serviceBinding) {
    this.serviceBinding = serviceBinding;
  }

  public String getSpecificationObject() {
    return specificationObject;
  }

  public void setSpecificationObject(String specificationObject) {
    this.specificationObject = specificationObject;
  }

  public String getUsageDescription() {
    return usageDescription;
  }

  public void setUsageDescription(String usageDescription) {
    this.usageDescription = usageDescription;
  }

  public List<String> getUsageParameters() {
    return usageParameters;
  }

  public void setUsageParameters(List<String> usageParameters) {
    this.usageParameters = usageParameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((serviceBinding == null) ? 0 : serviceBinding.hashCode());
    result = prime * result
        + ((specificationObject == null) ? 0 : specificationObject.hashCode());
    result = prime * result
        + ((usageDescription == null) ? 0 : usageDescription.hashCode());
    result = prime * result
        + ((usageParameters == null) ? 0 : usageParameters.hashCode());
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
    SpecificationLink other = (SpecificationLink) obj;
    if (serviceBinding == null) {
      if (other.serviceBinding != null)
        return false;
    } else if (!serviceBinding.equals(other.serviceBinding))
      return false;
    if (specificationObject == null) {
      if (other.specificationObject != null)
        return false;
    } else if (!specificationObject.equals(other.specificationObject))
      return false;
    if (usageDescription == null) {
      if (other.usageDescription != null)
        return false;
    } else if (!usageDescription.equals(other.usageDescription))
      return false;
    if (usageParameters == null) {
      if (other.usageParameters != null)
        return false;
    } else if (!usageParameters.equals(other.usageParameters))
      return false;
    return true;
  }

}
