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

/**
 * @author pramirez
 *
 */
public enum ObjectClass {
  ASSOCIATION("Association", Association.class),
  AUDITABLE_EVENT("AuditableEvent", AuditableEvent.class),
  CLASSIFICATION("Classification", Classification.class),
  CLASSIFICATION_NODE("ClassificationNode", ClassificationNode.class),
  CLASSIFICATION_SCHEME("ClassificationScheme", ClassificationScheme.class),
  EXTERNAL_IDENTIFIER("ExternalIdentifier", ExternalIdentifier.class),
  EXTERNAL_LINK("ExternalLink", ExternalLink.class),
  EXTRINSIC_OBJECT("ExtrinsicObject", ExtrinsicObject.class),
  REGISTRY_PACKAGE("RegistryPackage", RegistryPackage.class),
  SERVICE("Service", Service.class),
  SERVICE_BINDING("ServiceBinding", ServiceBinding.class),
  SPECIFICATION_LINK("SpecificationLink", SpecificationLink.class);
  
  private String name;
  private Class<? extends RegistryObject> clazz;
  
  ObjectClass(String name, Class<? extends RegistryObject> clazz) {
    this.name = name;
    this.clazz = clazz;
  }
  
  public static ObjectClass fromName(String name) {
    if (name != null) {
      for (ObjectClass objectClass : ObjectClass.values()) {
        if (name.equalsIgnoreCase(objectClass.name)) {
          return objectClass;
        }
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }
  
  public Class<? extends RegistryObject> getObjectClass() {
    return clazz;
  }
}
