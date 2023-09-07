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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author pramirez
 * 
 */
@XmlRootElement(name = "replicationReport", namespace = "http://registry.pds.nasa.gov")
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplicationReport implements Serializable {
  private static final long serialVersionUID = 8976639738002140711L;

  @XmlAttribute
  private ReplicationStatus status;

  @XmlAttribute
  private Date started;

  @XmlAttribute
  private long totalEvents;

  @XmlAttribute
  private long eventsProcessed;

  @XmlAttribute
  private Date lastModified;

  @XmlAttribute
  private String registryUrl;
  
  @XmlAttribute
  private String packageGuid;

  @XmlElement(name = "skippedEvent", namespace = "http://registry.pds.nasa.gov")
  private List<String> skippedEvents;

  @XmlElement(name = "skippedObject", namespace = "http://registry.pds.nasa.gov")
  private List<String> skippedObjects;

  public ReplicationReport() {
    super();
    started = new Date();
    lastModified = null;
    status = ReplicationStatus.RUNNING;
    skippedObjects = new ArrayList<String>();
    skippedEvents = new ArrayList<String>();
  }

  public ReplicationStatus getStatus() {
    return status;
  }

  public void setStatus(ReplicationStatus status) {
    this.status = status;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public long getTotalEvents() {
    return totalEvents;
  }

  public void setTotalEvents(long totalEvents) {
    this.totalEvents = totalEvents;
  }

  public long getEventsProcessed() {
    return eventsProcessed;
  }

  public void setEventsProcessed(long eventsProcessed) {
    this.eventsProcessed = eventsProcessed;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getRegistryUrl() {
    return registryUrl;
  }

  public void setRegistryUrl(String registryUrl) {
    this.registryUrl = registryUrl;
  }
  
  public String getPackageGuid() {
  	return packageGuid;
  }
  
  public void setPackageGuid(String packageGuid) {
  	this.packageGuid = packageGuid;
  }

  public List<String> getSkippedObjects() {
    return skippedObjects;
  }

  public void setSkippedObjects(List<String> skippedObjects) {
    this.skippedObjects = skippedObjects;
  }

  public void addSkippedObject(String skippedObject) {
    this.skippedObjects.add(skippedObject);
  }

  public List<String> getSkippedEvents() {
    return skippedEvents;
  }

  public void setSkippedEvents(List<String> skippedEvents) {
    this.skippedEvents = skippedEvents;
  }

  public void addSkippedEvent(String skippedEvent) {
    this.skippedEvents.add(skippedEvent);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (int) (eventsProcessed ^ (eventsProcessed >>> 32));
    result = prime * result
        + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((registryUrl == null) ? 0 : registryUrl.hashCode());
    result = prime * result
        + ((packageGuid == null) ? 0 : packageGuid.hashCode());
    result = prime * result
        + ((skippedObjects == null) ? 0 : skippedObjects.hashCode());
    result = prime * result
        + ((skippedEvents == null) ? 0 : skippedEvents.hashCode());
    result = prime * result + ((started == null) ? 0 : started.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + (int) (totalEvents ^ (totalEvents >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ReplicationReport other = (ReplicationReport) obj;
    if (eventsProcessed != other.eventsProcessed)
      return false;
    if (lastModified == null) {
      if (other.lastModified != null)
        return false;
    } else if (!lastModified.equals(other.lastModified))
      return false;
    if (registryUrl == null) {
      if (other.registryUrl != null)
        return false;
    } else if (!registryUrl.equals(other.registryUrl))
      return false;
    if (packageGuid == null) {
      if (other.packageGuid != null)
        return false;
    } else if (!packageGuid.equals(other.packageGuid))
      return false;
    if (skippedObjects == null) {
      if (other.skippedObjects != null)
        return false;
    } else if (!skippedObjects.equals(other.skippedObjects))
      return false;
    if (skippedEvents == null) {
      if (other.skippedEvents != null)
        return false;
    } else if (!skippedEvents.equals(other.skippedEvents))
      return false;
    if (started == null) {
      if (other.started != null)
        return false;
    } else if (!started.equals(other.started))
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    if (totalEvents != other.totalEvents)
      return false;
    return true;
  }

}
