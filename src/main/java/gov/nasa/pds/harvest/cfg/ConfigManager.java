package gov.nasa.pds.harvest.cfg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.glassfish.jaxb.runtime.v2.JAXBContextFactory;
import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.EstablishConnectionFactory;
import gov.nasa.pds.registry.common.meta.cfg.FileRefRule;

/**
 * Harvest configuration file reader.
 * 
 * @author karpenko
 */
public class ConfigManager
{
  private static HashMap<String,String> indexNodeMap = new HashMap<String,String>();
  static {
    indexNodeMap.put("pds-atm-registry", "PDS_ATM");
    indexNodeMap.put("pds-eng-registry", "PDS_ENG");
    indexNodeMap.put("pds-geo-registry", "PDS_GEO");
    indexNodeMap.put("pds-img-registry", "PDS_IMG");
    indexNodeMap.put("pds-naif-registry", "PDS_NAIF");
    indexNodeMap.put("pds-ppi-registry", "PDS_PPI");
    indexNodeMap.put("pds-rms-registry", "PDS_RMS");
    indexNodeMap.put("pds-sbn-registry", "pds_SBN");
    indexNodeMap.put("psa-registry", "PSA");
    indexNodeMap.put("jaxa-registry", "JAXA");
    indexNodeMap.put("roscosmos", "ROSCOSMOS");
  }
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
    static public String exchangeIndexForNode (String indexName) {
      if (indexNodeMap.containsKey (indexName)) return indexNodeMap.get(indexName);
      if (0 < indexName.indexOf("-registry")) return indexName.substring(0, indexName.indexOf("-registry"));
      return "development";
    }
    static public ConnectionFactory exchangeRegistry (RegistryType xml) throws Exception {
      return EstablishConnectionFactory.from (xml.getValue(), xml.getAuth());
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
