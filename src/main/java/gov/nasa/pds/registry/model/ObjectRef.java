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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "objectRef", namespace = "http://registry.pds.nasa.gov")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectRef extends Identifiable {

	private static final long serialVersionUID = 2162651275748309952L;

	@XmlAttribute
	private String guidRef;

	@XmlAttribute
	private String homeRef;

	public ObjectRef() {
	}

	/**
	 * @return the guidRef
	 */
	public String getGuidRef() {
		return guidRef;
	}

	/**
	 * @param guidRef
	 *            the guidRef to set
	 */
	public void setGuidRef(String guidRef) {
		this.guidRef = guidRef;
	}

	/**
	 * @return the homeRef
	 */
	public String getHomeRef() {
		return homeRef;
	}

	/**
	 * @param homeRef
	 *            the homeRef to set
	 */
	public void setHomeRef(String homeRef) {
		this.homeRef = homeRef;
	}

}
