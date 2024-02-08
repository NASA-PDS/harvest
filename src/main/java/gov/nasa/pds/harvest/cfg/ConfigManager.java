package gov.nasa.pds.harvest.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.glassfish.jaxb.runtime.v2.JAXBContextFactory;
import gov.nasa.pds.registry.common.cfg.RegistryCfg;
import gov.nasa.pds.registry.common.meta.cfg.FileRefRule;

/**
 * Harvest configuration file reader.
 * 
 * @author karpenko
 */
public class ConfigManager
{
    static public List<FileRefRule> exchangeFileRef (List<FileRefType> xml2beans) {
      ArrayList<FileRefRule> beans = new ArrayList<FileRefRule>();
      FileRefRule rule;
      for (FileRefType xml : xml2beans) {
        rule = new FileRefRule();
        rule.prefix = xml.getReplacePrefix();
        rule.replacement = xml.getWith();
        beans.add (rule);
      }
      return beans;
    }
    static public List<String> exchangeLids (List<CollectionType> ids) {
      ArrayList<String> lids = new ArrayList<String>();
      for (CollectionType id : ids) {
        if (0 < id.getLid().length()) lids.add (id.getLid());
      }
      return lids;
    }
    static public List<String> exchangeLidvids (List<CollectionType> ids) {
      ArrayList<String> lidvids = new ArrayList<String>();
      for (CollectionType id : ids) {
        if (0 < id.getLidvid().length()) lidvids.add (id.getLidvid());
      }
      return lidvids;
    }
    /**
     * FIXME: This is a hack to keep changes limited to harvest.
     *        Replace the whole registry initiation with something different
     *        when moving to multitenancy 
     * @param xml2bean
     * @return
     */
    static public RegistryCfg exchangeRegistry (RegistryType xml) {
      RegistryCfg bean = new RegistryCfg();
      bean.authFile = xml.getAuth();
      bean.indexName = xml.getIndex();
      bean.url = xml.getServerUrl();
      return bean;
    }
    static public HarvestConfigurationType read(File file) throws JAXBException {
      JAXBContext jaxbContext = new JAXBContextFactory().createContext(new Class[]{Harvest.class}, null);
      HarvestConfigurationType result = (HarvestConfigurationType)jaxbContext.createUnmarshaller().unmarshal(file);
      ObjectFactory forge = new ObjectFactory();
      if (result.getAutogenFields() == null) {
        result.setAutogenFields(forge.createAutogenFieldsType());
      }
      if (result.getAutogenFields().getClassFilter() == null) {
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
