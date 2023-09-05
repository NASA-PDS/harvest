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

public class AssociationFilter extends ObjectFilter {
	private String sourceObject;
	private String targetObject;
	private String associationType;
	
	private AssociationFilter() {
		super();
	}
	
	public static class Builder extends AbstractBuilder {
		private AssociationFilter filter;
		
		public Builder() {
			filter = new AssociationFilter();
		}
		
		public Builder sourceObject(String sourceObject) {
			this.checkBuilt();
			this.filter.sourceObject = sourceObject;
			return this;
		}
		
		public Builder targetObject(String targetObject) {
			this.checkBuilt(); 
			this.filter.targetObject = targetObject;
			return this;
		}
		
		public Builder associationType(String associationType) {
			this.checkBuilt();
			this.filter.associationType = associationType;
			return this;
		}
		
		public AssociationFilter build() {
			this.checkBuilt();
			this.isBuilt = true;
			return this.filter;
		}
	}

	public String getSourceObject() {
		return sourceObject;
	}

	public String getTargetObject() {
		return targetObject;
	}

	public String getAssociationType() {
		return associationType;
	}
}
