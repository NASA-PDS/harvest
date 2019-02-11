// Copyright 2006-2010, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
package gov.nasa.pds.harvest.search.registry;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An exception when ingesting a product.</p>
 *
 */
public class RegistryClientException extends Exception {
  private static final long serialVersionUID = 4134078866650777281L;

    public RegistryClientException(String message) {
        super(message);
    }
}
