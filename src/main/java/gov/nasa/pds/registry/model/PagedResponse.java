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

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A class to wrap list responses from the registry with.
 * 
 * @author pramirez
 * 
 */
@XmlRootElement(name = "response", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class PagedResponse<T> implements Serializable {
  
  private static final long serialVersionUID = 2848386465677347398L;

  // Where in the set of results to start
  @XmlAttribute
  private Integer start;

  // How many items were found
  @XmlAttribute
  private Long numFound;

  @XmlElementWrapper(name = "results", namespace = "http://registry.pds.nasa.gov")
  @XmlElements({
    @XmlElement(name="association", type=Association.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="auditableEvent", type=AuditableEvent.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="classification", type=Classification.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="classificationNode", type=ClassificationNode.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="classificationScheme", type=ClassificationScheme.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="externalIdentifier", type=ExternalIdentifier.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="extrinsicObject", type=ExtrinsicObject.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="registryPackage", type=RegistryPackage.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="service", type=Service.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="serviceBinding", type=ServiceBinding.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="slot", type=Slot.class, namespace="http://registry.pds.nasa.gov"),
    @XmlElement(name="specificationLink", type=SpecificationLink.class, namespace="http://registry.pds.nasa.gov")
  })
  private List<T> results;

  public PagedResponse() {
  }

  public PagedResponse(Integer start, Long numFound) {
    this(start, numFound, null);
  }

  public PagedResponse(List<T> results) {
    this(null, null, results);
  }

  public PagedResponse(Integer start, Long numFound,
      List<T> results) {
    this.start = start;
    this.numFound = numFound;
    this.results = results;
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public Long getNumFound() {
    return numFound;
  }

  public void setNumFound(Long numFound) {
    this.numFound = numFound;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }
}
