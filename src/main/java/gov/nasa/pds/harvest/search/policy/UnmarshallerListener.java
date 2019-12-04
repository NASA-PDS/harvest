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

package gov.nasa.pds.harvest.search.policy;

import gov.nasa.pds.harvest.search.util.Utility;

import javax.xml.bind.Unmarshaller.Listener;

/**
 * Listener class that is used during the unmarshalling process
 * to resolve environment variables that might be defined within a Policy
 * file.
 *
 * @author mcayanan
 *
 */
public class UnmarshallerListener extends Listener {

  /**
   * Resolves environment variables that could be found in one of
   * the following elements in the policy file:
   *
   * <ul>
   *   <li>path within a Directory or Pds3Directory Element</li>
   *   <li>manifest within a Checksum Element</li>
   *   <li>file within a Collection Element</li>
   *   <li>offset within an AccessUrl Element</li>
   * </ul>
   *
   */
  public void afterUnmarshal(Object target, Object parent) {
    if (target instanceof Directory) {
      Directory dir = (Directory) target;
      if (dir.path != null) {
        dir.path = Utility.resolveEnvVars(dir.path);
      }
    } else if (target instanceof Checksums) {
      Checksums checksums = (Checksums) target;
      if (checksums.getManifest() != null) {
        Manifest cm = checksums.getManifest();
        if (cm.value != null) {
          cm.value = Utility.resolveEnvVars(cm.value);
        }
        if (cm.basePath != null) {
          cm.basePath = Utility.resolveEnvVars(cm.basePath);
        }
      }
    } else if (target instanceof Pds3Directory) {
      Pds3Directory dir = (Pds3Directory) target;
      if (dir.path != null) {
        dir.path = Utility.resolveEnvVars(dir.path);
      }
    } else if (target instanceof Collection) {
      Collection collection = (Collection) target;
      if (collection.file != null) {
        collection.file = Utility.resolveEnvVars(collection.file);
      }
    } else if (target instanceof AccessUrl) {
      AccessUrl url = (AccessUrl) target;
      if (url.offset != null) {
        url.offset = Utility.resolveEnvVars(url.offset);
      }
    } else if (target instanceof LidContents) {
      LidContents lid = (LidContents) target;
      if (lid.offset != null) {
        lid.offset = Utility.resolveEnvVars(lid.offset);
      }
    }
  }
}
