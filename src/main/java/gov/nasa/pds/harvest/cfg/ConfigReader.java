package gov.nasa.pds.harvest.cfg;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.glassfish.jaxb.runtime.v2.JAXBContextFactory;

/**
 * Harvest configuration file reader.
 * 
 * @author karpenko
 */
public class ConfigReader
{
    /**
     * Constructor
     */
    public ConfigReader() {
    }
    
    public HarvestConfigurationType read(File file) throws JAXBException {
      JAXBContext jaxbContext = new JAXBContextFactory().createContext(new Class[]{HarvestConfigurationType.class}, null);
      HarvestConfigurationType result = (HarvestConfigurationType)jaxbContext.createUnmarshaller().unmarshal(file);
      ObjectFactory forge = new ObjectFactory();
      if (result.getAutogenFields() == null) {
        result.setAutogenFields(forge.createAutogenFieldsType());
        result.getAutogenFields().setClassFilter(forge.createFilterType());
      }
      if (result.getFileInfo() == null) {
        result.setFileInfo(forge.createFileInfoType());
      }
      if (result.getProductFilter() == null) {
        result.setProductFilter(forge.createFilterType());
      }
      if (result.getReferences() == null) {
        result.setReferences(forge.createReferencesType());
      }
      if (result.getXpathMaps() == null) {
        result.setXpathMaps(forge.createXpathMapsType());
      }
      return result;
    }
}
