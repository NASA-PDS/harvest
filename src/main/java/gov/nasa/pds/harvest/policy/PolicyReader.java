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

package gov.nasa.pds.harvest.policy;

import gov.nasa.pds.harvest.util.XMLValidationEventHandler;

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PolicyReader {
    public final static String POLICY_PACKAGE = "gov.nasa.pds.harvest.policy";
    public final static String POLICY_SCHEMA = "harvest-policy.xsd";

    public static Policy unmarshall(InputStream policyXML)
    throws SAXParseException, JAXBException, SAXException {
        return unmarshall(new StreamSource(policyXML));
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
        um.setSchema(schema);
        um.setEventHandler(new XMLValidationEventHandler());
        JAXBElement<Policy> policy = um.unmarshal(policyXML, Policy.class);
        return policy.getValue();
    }
}
