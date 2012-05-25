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
package gov.nasa.pds.harvest.util;

import gov.nasa.pds.registry.model.Association;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.model.RegistryObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Utility class.
 *
 */
public class Utility {

    /**
     * Convert a string to a URL.
     *
     * @param s The string to convert
     * @return A URL of the input string
     */
    public static URL toURL(String s) throws MalformedURLException {
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException ex) {
            url = new File(s).toURI().toURL();
        }
        return url;
    }

    /**
     * Convert a string to a URI.
     *
     * @param s The string to convert.
     *
     * @return A well-formed URI.
     */
    public static String toWellFormedURI(String s) {
        return s.replaceAll(" ", "%20");
    }

    /**
     * Get the current date time.
     *
     * @return A date time.
     */
    public static String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat(
        "EEE, MMM dd yyyy 'at' hh:mm:ss a");
        Date date = Calendar.getInstance().getTime();
        return df.format(date);
    }

    /**
     * Convert the ExtrinsicObject into an XML.
     *
     * @param extrinsic The ExtrinsicObject.
     *
     * @return The XML representation of the given ExtrinsicObject.
     *
     * @throws JAXBException If there was an error marshalling the given
     *  object.
     */
    public static String toXML(ExtrinsicObject extrinsic) throws JAXBException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      JAXBContext jc = JAXBContext.newInstance(ExtrinsicObject.class);
      //Create marshaller
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      //Marshal object into file.
      m.marshal(extrinsic, output);
      return output.toString();
    }

    /**
     * Convert the Association into an XML.
     *
     * @param association The Association.
     *
     * @return The XML representation of the given Association.
     *
     * @throws JAXBException If there was an error marshalling the given
     *  association.
     */
    public static String toXML(Association association) throws JAXBException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      JAXBContext jc = JAXBContext.newInstance(Association.class);
      //Create marshaller
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      //Marshal object into file.
      m.marshal(association, output);
      return output.toString();
    }
}
