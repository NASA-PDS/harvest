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

import gov.nasa.pds.harvest.search.util.XMLValidationEventHandler;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class to read the Harvest Policy file.
 * 
 * @author mcayanan
 *
 */
public class PolicyReader {
    public final static String POLICY_PACKAGE = "gov.nasa.pds.harvest.search.policy";
    public final static String POLICY_SCHEMA = "harvest-policy.xsd";

    public static Policy unmarshall(URL policyXML)
    throws SAXParseException, JAXBException, SAXException {
      return unmarshall(new StreamSource(policyXML.toString()));
    }

    public static Policy unmarshall(File policyXML)
    throws SAXParseException, JAXBException, SAXException {
        return unmarshall(new StreamSource(policyXML));
    }

    public static Policy unmarshall(StreamSource policyXML)
    throws JAXBException, SAXException, SAXParseException {
        JAXBContext jc = JAXBContext.newInstance(POLICY_PACKAGE);
        Unmarshaller um = jc.createUnmarshaller();
        SchemaFactory sf = SchemaFactory.newInstance(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = sf.newSchema(
                    PolicyReader.class.getResource(POLICY_SCHEMA));
        } catch (SAXException se) {
            throw new SAXException("Problems parsing harvest policy schema: "
                    + se.getMessage());
        }
        um.setListener(new UnmarshallerListener());
        um.setSchema(schema);
        um.setEventHandler(new XMLValidationEventHandler(
            policyXML.getSystemId()));
        JAXBElement<Policy> policy = um.unmarshal(policyXML, Policy.class);
        return policy.getValue();
    }
}
