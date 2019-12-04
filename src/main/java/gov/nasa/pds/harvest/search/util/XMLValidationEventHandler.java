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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import gov.nasa.pds.harvest.search.logging.ToolsLevel;
import gov.nasa.pds.harvest.search.logging.ToolsLogRecord;

public class XMLValidationEventHandler implements ValidationEventHandler {
    private static Logger log = Logger.getLogger(
            XMLValidationEventHandler.class.getName());
    private String systemId;

    public XMLValidationEventHandler(String systemId) {
      this.systemId = systemId;
    }

    public boolean handleEvent(ValidationEvent event) {
        Level level = null;
        if(event.getSeverity() == ValidationEvent.ERROR
                || event.getSeverity() == ValidationEvent.FATAL_ERROR) {
            level = ToolsLevel.SEVERE;
        } else if(event.getSeverity() == ValidationEvent.WARNING) {
            level = ToolsLevel.WARNING;
        }
        if (event.getLocator().getURL() != null) {
          log.log(new ToolsLogRecord(level, event.getMessage(),
                event.getLocator().getURL().toString(),
                event.getLocator().getLineNumber()));
        } else {
          log.log(new ToolsLogRecord(level, event.getMessage(),
              systemId, event.getLocator().getLineNumber()));
        }
        return false;
    }

}
