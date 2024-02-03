//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.3 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package gov.nasa.pds.harvest.cfg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         Currently, connecting to an opensearch service directly requires just
 *         two pieces of information:
 *           1. URL: the URL to directly contact an opensearch instance and its
 *                   API to include authentication
 *           2. @trustSelfSigned: when running locally for testing or development
 *                                it is often necessary to use self signed
 *                                certificates. Setting this option to true
 *                                will all for self signed certificates.
 *       
 * 
 * <p>Java class for direct_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>{@code
 * <complexType name="direct_type">
 *   <simpleContent>
 *     <extension base="<http://www.w3.org/2001/XMLSchema>normalizedString">
 *       <attribute name="trust_self_signed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "direct_type", propOrder = {
    "value"
})
public class DirectType {

    @XmlValue
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String value;
    @XmlAttribute(name = "trust_self_signed")
    protected Boolean trustSelfSigned;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the trustSelfSigned property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTrustSelfSigned() {
        if (trustSelfSigned == null) {
            return false;
        } else {
            return trustSelfSigned;
        }
    }

    /**
     * Sets the value of the trustSelfSigned property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTrustSelfSigned(Boolean value) {
        this.trustSelfSigned = value;
    }

}
