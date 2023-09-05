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
import gov.nasa.pds.registry.model.Slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class leverages the Decorator Pattern by inheriting the functions of
 * {@link ExtrinsicObject} through the {@link ExtrinsicObjectDecorator} class.
 * Currently no methods are overridden, however, this provides that flexibility
 * that should be a part of extending a class.
 * 
 * @author jpadams
 *
 */
public class ExtendedExtrinsicObject extends ExtrinsicObjectDecorator {
	private static final long serialVersionUID = 2780915859620067707L;
	
	/** Attribute for version of a product. **/
	public static final String VERSION_ID_SLOT = "version_id";
	
	private boolean validAssociationValues;
	private List<String> invalidAssociations;
	
	public ExtendedExtrinsicObject(ExtrinsicObject extObject) {
		super(extObject);
		
		this.validAssociationValues = true;
		this.invalidAssociations = new ArrayList<String>();
	}
	
	/**
	 * Returns a list of Strings that pertain to a particular slot
	 * 
	 * @param slotName
	 * @return
	 * @throws SearchCoreFatalException
	 */
	public List<String> getSlotValues(String slotName) {		
		Slot slot = super.getSlot(slotName.trim());
		RegistryAttributeWrapper ra;
		
		// This if-else handles whether the slotName is a slot,
		// attribute, or missing altogether.
		if (slot != null) {	// Slot exists
			
			// Check slot is an association ref (contains _ref)
			if (slotIsAssociationReference(slotName)) {
				// If it is an association ref, loop through the values
				for (String slotValue : slot.getValues()) {
					// If ref is not a lidvid, this is invalid
					if (!slotValueIsLidvid(slotValue)) {
						this.validAssociationValues = false;
					}
				}
				
				// If any association refs are invalid, note for later
				if (!this.validAssociationValues) {
					addInvalidAssociation(slotName);
				}
			}
			
			// Regardless of what the slot name is, return all its values
			return slot.getValues();
		} else if ((ra = RegistryAttributeWrapper.get(slotName)) != null) {
			return Arrays.asList(ra.getValueFromExtrinsic(super.decoratedExtrinsic));
		} else {
			//SearchCoreStats.addMissingSlot(super.getLid(), slotName);
			return null;
		}
	}
	
	public String getLidvid() {
		List<String> versionValues = getSlotValues(VERSION_ID_SLOT);
		if (versionValues != null) {
			return super.getLid() + "::" + versionValues.get(0);
		} else {
			return super.getLid();
		}
	}
	
	/**
	 * Check if slot is an association reference by checking if
	 * the slot name ends with a "_ref"
	 * 
	 * @param slotName
	 * @return
	 */
	public boolean slotIsAssociationReference(String slotName) {
		if (slotName.endsWith("_ref")) {
			return true;
		} else {
			return false;
		}
	}
	
	/** 
	 * Check is slot is a lidvid. By PDS4 model definition,
	 * lidvids are the only slot values that will contain ::
	 * 
	 * @param slotValue
	 * @return
	 */
	public boolean slotValueIsLidvid(String slotValue) {
		if (slotValue.contains("::")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasValidAssociationValues() {
		return this.validAssociationValues;
	}
	
	public void setValidAssociationValues(boolean validAssociationValues) {
		this.validAssociationValues = validAssociationValues;
	}
	
	public void addInvalidAssociation(String slotName) {
		if (!this.invalidAssociations.contains(slotName)) {
			this.invalidAssociations.add(slotName);
		}
	}
	
	/**
	 * Utility method to convert list of ExtrinsicObjects to SearchCoreExtrinsic objects
	 * 
	 * @param extObjList
	 * @return
	 */
	public static List<ExtendedExtrinsicObject> asSearchCoreExtrinsics(List<ExtrinsicObject> extObjList) {
		List<ExtendedExtrinsicObject> sceList = new ArrayList<ExtendedExtrinsicObject>();
		for (ExtrinsicObject extObj : extObjList) {
			sceList.add(new ExtendedExtrinsicObject(extObj));
		}
		return sceList;
	}
	
}
