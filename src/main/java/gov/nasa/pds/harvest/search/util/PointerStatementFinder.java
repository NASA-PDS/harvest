// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// * Neither the name of Caltech nor its operating division, the Jet Propulsion
// Laboratory, nor the names of its contributors may be used to endorse or
// promote products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package gov.nasa.pds.harvest.search.util;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.pds.tools.label.Label;
import gov.nasa.pds.tools.label.ObjectStatement;
import gov.nasa.pds.tools.label.PointerStatement;

/**
 * Class that recursively finds pointer statements in a PDS3 label.
 *
 * @author mcayanan
 *
 */
public class PointerStatementFinder {
  /**
   * Finds all pointer statements in a given label.
   *
   * @param label A PDS3 label.
   *
   * @return A list of pointer statements found within the given label.
   */
  static public List<PointerStatement> find(Label label) {
    List<PointerStatement> result = new ArrayList<PointerStatement>();
    result.addAll(label.getPointers());
    for (ObjectStatement object : label.getObjects()) {
      result.addAll(find(object));
    }
    return result;
  }

  /**
   * Finds all pointer statements in a given object statement.
   *
   * @param object An object statement.
   *
   * @return A list of pointer statements found within a given label.
   */
  static private List<PointerStatement> find(ObjectStatement object) {
    List<PointerStatement> result = new ArrayList<PointerStatement>();
    result.addAll(object.getPointers());
    for (ObjectStatement o : object.getObjects()) {
      result.addAll(find(o));
    }
    return result;
  }
}
