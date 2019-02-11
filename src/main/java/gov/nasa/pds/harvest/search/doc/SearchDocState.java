// Copyright 2006-2016, by the California Institute of Technology.
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

package gov.nasa.pds.harvest.search.doc;

public class SearchDocState {

  private int counter = -1;
  
  public int getCounter() {
    return this.counter;
  }

  public void setCounter(int counter) {
    this.counter = counter;
  }

  public void incrementCounter() {
    this.counter++;
  }
  
}
