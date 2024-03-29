//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.3 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package gov.nasa.pds.harvest.cfg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         Define the connection to the registry, security for the connection, and
 *         the index within the registry.
 * 
 *         @auth: a java property file containing a username and password
 *         @index: the index to be used by harvest whose default is registry
 *         @trust_self_signed: all self signed certificates for https
 * 
 *         cognito_client_id: the cognito client ID for AWS based instances of opensearch
 *         server_url: the opensearch URL when not using AWS services
 *       
 * 
 * <p>Java class for registry_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>{@code
 * <complexType name="registry_type">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="cognito_client_id" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         <element name="server_url" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *       </choice>
 *       <attribute name="auth" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *       <attribute name="index" type="{http://www.w3.org/2001/XMLSchema}normalizedString" default="registry" />
 *       <attribute name="trust_self_signed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "registry_type", propOrder = {
    "cognitoClientId",
    "serverUrl"
})
public class RegistryType {

    @XmlElement(name = "cognito_client_id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String cognitoClientId;
    @XmlElement(name = "server_url")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String serverUrl;
    @XmlAttribute(name = "auth", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String auth;
    @XmlAttribute(name = "index")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String index;
    @XmlAttribute(name = "trust_self_signed")
    protected Boolean trustSelfSigned;

    /**
     * Gets the value of the cognitoClientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCognitoClientId() {
        return cognitoClientId;
    }

    /**
     * Sets the value of the cognitoClientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCognitoClientId(String value) {
        this.cognitoClientId = value;
    }

    /**
     * Gets the value of the serverUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Sets the value of the serverUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServerUrl(String value) {
        this.serverUrl = value;
    }

    /**
     * Gets the value of the auth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuth() {
        return auth;
    }

    /**
     * Sets the value of the auth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuth(String value) {
        this.auth = value;
    }

    /**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndex() {
        if (index == null) {
            return "registry";
        } else {
            return index;
        }
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndex(String value) {
        this.index = value;
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
