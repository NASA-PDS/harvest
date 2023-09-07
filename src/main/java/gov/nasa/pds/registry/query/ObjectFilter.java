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

package gov.nasa.pds.registry.query;

import gov.nasa.pds.registry.model.ObjectStatus;

/**
 * This class identifies all the attributes one would filter on for any type of {@link RegistryObject}. This is used to build up a query from classes that extend {@link RegistryQuery}
 * @author pramirez
 *
 */
public class ObjectFilter {
	protected String guid;
	protected String name;
	protected String lid;
	protected String versionName;
	protected String objectType;
	protected ObjectStatus status;
	
	protected ObjectFilter() {
	}
	
	public static class Builder extends AbstractBuilder {
		private ObjectFilter filter;
		
		public Builder() {
			filter = new ObjectFilter();
		}
		
		public Builder guid(String guid) {
			this.checkBuilt();
			this.filter.guid = guid;
			return this;
		}
		
		public Builder name(String name) {
			this.checkBuilt();
			this.filter.name = name;
			return this;
		}
		
		public Builder lid(String lid) {
			this.checkBuilt();
			this.filter.lid = lid;
			return this;
		}
		
		public Builder versionName(String versionName) {
			this.checkBuilt();
			this.filter.versionName = versionName;
			return this;
		}
		
		public Builder objectType(String objectType) {
			this.checkBuilt();
			this.filter.objectType = objectType;
			return this;
		}
		
		public Builder status(ObjectStatus status) {
			this.checkBuilt();
			this.filter.status = status;
			return this;
		}
    
		public ObjectFilter build() {
			this.checkBuilt();
			this.isBuilt = true;
			return this.filter;
		}
		
	}

	public String getGuid() {
		return guid;
	}

	public String getName() {
		return name;
	}

	public String getLid() {
		return lid;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getObjectType() {
		return objectType;
	}

	public ObjectStatus getStatus() {
		return status;
	}
}
