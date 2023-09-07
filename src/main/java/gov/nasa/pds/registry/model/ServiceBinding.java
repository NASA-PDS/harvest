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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * ServiceBinding instances are RegistryObjects that represent technical
 * information on a specific way to access a Service instance. An example is
 * where a ServiceBinding is defined for each protocol that may be used to
 * access the service.
 * 
 * @author pramirez
 * 
 */
@Entity
@XmlRootElement(name = "serviceBinding", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceBinding extends RegistryObject {

  private static final long serialVersionUID = 6284810355021108112L;

  /**
   * A ServiceBinding MAY have an accessURI attribute that defines the URI to
   * access that ServiceBinding. This attribute is ignored if a targetBinding
   * attribute is specified for the ServiceBinding. If the URI is a URL then a
   * registry MUST validate the URL to be resolvable at the time of submission
   * before accepting a ServiceBinding submission to the registry.
   */
  @XmlAttribute
  private String accessURI;

  /**
   * A ServiceBinding MUST have a service attribute whose value MUST be the guid
   * of its parent Service.
   */
  @XmlAttribute
  private String service;

  /**
   * A ServiceBinding MAY have a specificationLinks attribute defined that is a
   * Set of references to SpecificationLink instances. Each SpecificationLink
   * instance links the ServiceBinding to a particular technical specification
   * that MAY be used to access the Service for the ServiceBinding.
   */
  @OneToMany(cascade = CascadeType.ALL)
  @XmlElementRef(name = "specificationLink", namespace = "http://registry.pds.nasa.gov")
  @OrderBy
  private Set<SpecificationLink> specificationLinks;

  /**
   * A ServiceBinding MAY have a targetBinding attribute defined that references
   * another ServiceBinding by its guid. A targetBinding MAY be specified when a
   * service is being redirected to another service. This allows the rehosting
   * of a service by another service provider.
   */
  @XmlAttribute
  private String targetBinding;
  
  public ServiceBinding() {
    this.setObjectType(ServiceBinding.class.getSimpleName());
  }
  
  public String getAccessURI() {
    return accessURI;
  }

  public void setAccessURI(String accessURI) {
    this.accessURI = accessURI;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public Set<SpecificationLink> getSpecificationLinks() {
    return specificationLinks;
  }

  public void setSpecificationLinks(Set<SpecificationLink> specificationLinks) {
    this.specificationLinks = specificationLinks;
  }

  public String getTargetBinding() {
    return targetBinding;
  }

  public void setTargetBinding(String targetBinding) {
    this.targetBinding = targetBinding;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((accessURI == null) ? 0 : accessURI.hashCode());
    result = prime * result + ((service == null) ? 0 : service.hashCode());
    result = prime * result
        + ((specificationLinks == null) ? 0 : specificationLinks.hashCode());
    result = prime * result
        + ((targetBinding == null) ? 0 : targetBinding.hashCode());
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
    ServiceBinding other = (ServiceBinding) obj;
    if (accessURI == null) {
      if (other.accessURI != null)
        return false;
    } else if (!accessURI.equals(other.accessURI))
      return false;
    if (service == null) {
      if (other.service != null)
        return false;
    } else if (!service.equals(other.service))
      return false;
    if (specificationLinks == null) {
      if (other.specificationLinks != null)
        return false;
    } else if (!specificationLinks.equals(other.specificationLinks))
      return false;
    if (targetBinding == null) {
      if (other.targetBinding != null)
        return false;
    } else if (!targetBinding.equals(other.targetBinding))
      return false;
    return true;
  }

}
