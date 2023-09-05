//	Copyright 2013-2014, by the California Institute of Technology.
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
package gov.nasa.pds.registry.model.wrapper;

import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.ObjectStatus;
import gov.nasa.pds.registry.query.ExtrinsicFilter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the values for a given attribute specified in a product class
 * configuration file.
 * 
 * @author jpadams
 * @version $Revision: 11832 $
 * 
 */
public enum RegistryAttributeWrapper  {
	/** Attribute for Logical Identifier. **/
	LOGICAL_IDENTIFIER ("lid") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getLid();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getLid());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.lid(value);
		}
	},

	/** Attribute for Product name. **/
	NAME ("name") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getName();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getName());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.name(value);
		}
	},

	/** Attribute for Object Type. **/
	OBJECT_TYPE ("objectType") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getObjectType();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getObjectType());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.objectType(value);
		}
	},

	/** Attribute for MIME Type. **/
	MIME_TYPE ("mimeType") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getMimeType();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getMimeType());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.mimeType(value);
		}
	},

	/** Attribute for Description. **/
	DESCRIPTION ("description") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getDescription();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getDescription());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return null;	// Cannot filter on description
		}
	},

	/** Attribute for GUID. **/
	GUID ("guid") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getGuid();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getGuid());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.guid(value);
		}
	},

	/** Attribute for Home. **/
	HOME ("home") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getHome();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getHome());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return null;	// Not yet implemented by Registry
		}
	},

	/** Attribute for Version Name. **/
	VERSION_NAME ("versionName") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getVersionName();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getVersionName());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.versionName(value);
		}
	},

	/** Attribute for Content Version. **/
	CONTENT_VERSION ("contentVersion") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getContentVersion();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getContentVersion());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.contentVersion(value);
		}
	},
		
	/** Attribute for Product status. **/
	STATUS ("status") {
		@Override
		public String getValueFromExtrinsic(ExtrinsicObject extObj) {
			return extObj.getStatus().name();
		}
		
		@Override
		public List<String> getValuesFromExtrinsic(ExtrinsicObject extObj) {
			return Arrays.asList(extObj.getStatus().name());
		}
		
		@Override
		public ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value) {
			return builder.status(ObjectStatus.valueOf(value));
		}
	};

	private String attributeName;
    private static final Map<String, RegistryAttributeWrapper> lookup = new HashMap<String, RegistryAttributeWrapper>();

	static {
	    for(RegistryAttributeWrapper ra : EnumSet.allOf(RegistryAttributeWrapper.class))
	         lookup.put(ra.getName(), ra);
	}
	
	private RegistryAttributeWrapper(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getName() {
		return this.attributeName;
	}
	
	public static RegistryAttributeWrapper get(String attributeName) {
		return lookup.get(attributeName);
	}
	
	public abstract String getValueFromExtrinsic(ExtrinsicObject extObj);
	
	public abstract List<String> getValuesFromExtrinsic(ExtrinsicObject extObj);
	
	public abstract ExtrinsicFilter.Builder buildOntoFilter(ExtrinsicFilter.Builder builder, String value);
	
}
