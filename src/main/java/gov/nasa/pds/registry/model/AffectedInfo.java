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

/**
 * @author pramirez
 *
 */
public class AffectedInfo {
  private List<String> _affectedIds;
  private List<String> _affectedTypes;
  
  public AffectedInfo() {
  }
  
  public AffectedInfo(List<String> affectedIds, List<String> affectedTypes) {
    _affectedIds = affectedIds;
    _affectedTypes = affectedTypes;
  }
  
  public List<String> getAffectedIds() {
    return _affectedIds;
  }
  
  public List<String> getAffectedTypes() {
    return _affectedTypes;
  }

}
