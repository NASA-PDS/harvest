//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.3 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package gov.nasa.pds.harvest.cfg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *         These are the basic options for the harvest configuration file.
 * 
 * 
 *         autogenFields: should not be used except in development testing
 *         load: tells where and how to harvest PDS4 labels
 *         fileInfo: option allowing filename prefixes to be replaced
 *         nodeName: the PDS node that this harvest run applies to
 *         productFilter: should not be used except in development testing
 *         registry: define the server harvest should use
 *         xpathMaps: allow constraints in the PDS4 label to control harvesting
 *       
 * 
 * <p>Java class for harvest_configuration_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>{@code
 * <complexType name="harvest_configuration_type">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <all>
 *         <element name="autogenFields" type="{}autogen_fields_type" minOccurs="0"/>
 *         <element name="load" type="{}load_type"/>
 *         <element name="fileInfo" type="{}file_info_type" minOccurs="0"/>
 *         <element name="productFilter" type="{}filter_type" minOccurs="0"/>
 *         <element name="references" type="{}references_type" minOccurs="0"/>
 *         <element name="registry" type="{}registry_type"/>
 *         <element name="xpathMaps" type="{}xpath_maps_type" minOccurs="0"/>
 *       </all>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "harvest_configuration_type", propOrder = {

})
@XmlSeeAlso({
    Harvest.class
})
public class HarvestConfigurationType {

    protected AutogenFieldsType autogenFields;
    @XmlElement(required = true)
    protected LoadType load;
    protected FileInfoType fileInfo;
    protected FilterType productFilter;
    protected ReferencesType references;
    @XmlElement(required = true)
    protected RegistryType registry;
    protected XpathMapsType xpathMaps;

    /**
     * Gets the value of the autogenFields property.
     * 
     * @return
     *     possible object is
     *     {@link AutogenFieldsType }
     *     
     */
    public AutogenFieldsType getAutogenFields() {
        return autogenFields;
    }

    /**
     * Sets the value of the autogenFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link AutogenFieldsType }
     *     
     */
    public void setAutogenFields(AutogenFieldsType value) {
        this.autogenFields = value;
    }

    /**
     * Gets the value of the load property.
     * 
     * @return
     *     possible object is
     *     {@link LoadType }
     *     
     */
    public LoadType getLoad() {
        return load;
    }

    /**
     * Sets the value of the load property.
     * 
     * @param value
     *     allowed object is
     *     {@link LoadType }
     *     
     */
    public void setLoad(LoadType value) {
        this.load = value;
    }

    /**
     * Gets the value of the fileInfo property.
     * 
     * @return
     *     possible object is
     *     {@link FileInfoType }
     *     
     */
    public FileInfoType getFileInfo() {
        return fileInfo;
    }

    /**
     * Sets the value of the fileInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileInfoType }
     *     
     */
    public void setFileInfo(FileInfoType value) {
        this.fileInfo = value;
    }

    /**
     * Gets the value of the productFilter property.
     * 
     * @return
     *     possible object is
     *     {@link FilterType }
     *     
     */
    public FilterType getProductFilter() {
        return productFilter;
    }

    /**
     * Sets the value of the productFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterType }
     *     
     */
    public void setProductFilter(FilterType value) {
        this.productFilter = value;
    }

    /**
     * Gets the value of the references property.
     * 
     * @return
     *     possible object is
     *     {@link ReferencesType }
     *     
     */
    public ReferencesType getReferences() {
        return references;
    }

    /**
     * Sets the value of the references property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferencesType }
     *     
     */
    public void setReferences(ReferencesType value) {
        this.references = value;
    }

    /**
     * Gets the value of the registry property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryType }
     *     
     */
    public RegistryType getRegistry() {
        return registry;
    }

    /**
     * Sets the value of the registry property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryType }
     *     
     */
    public void setRegistry(RegistryType value) {
        this.registry = value;
    }

    /**
     * Gets the value of the xpathMaps property.
     * 
     * @return
     *     possible object is
     *     {@link XpathMapsType }
     *     
     */
    public XpathMapsType getXpathMaps() {
        return xpathMaps;
    }

    /**
     * Sets the value of the xpathMaps property.
     * 
     * @param value
     *     allowed object is
     *     {@link XpathMapsType }
     *     
     */
    public void setXpathMaps(XpathMapsType value) {
        this.xpathMaps = value;
    }

}
