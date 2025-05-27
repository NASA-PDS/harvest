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
import gov.nasa.pds.registry.common.util.AccessRights;
import gov.nasa.pds.registry.common.util.CompressionPattern;

/**
 * Harvest configuration file reader.
 * 
 * @author karpenko
 */
public class ConfigManager
{
  private static HashMap<String,String> indexNodeMap = new HashMap<String,String>();
  static {
    indexNodeMap.put("atm-registry", "PDS_ATM");
    indexNodeMap.put("en-registry", "PDS_ENG");
    indexNodeMap.put("geo-registry", "PDS_GEO");
    indexNodeMap.put("img-registry", "PDS_IMG");
    indexNodeMap.put("naif-registry", "PDS_NAIF");
    indexNodeMap.put("ppi-registry", "PDS_PPI");
    indexNodeMap.put("rms-registry", "PDS_RMS");
    indexNodeMap.put("sbnpsi-registry", "PDS_SBN");
    indexNodeMap.put("sbnumd-registry", "PDS_SBN");
    indexNodeMap.put("psa-registry", "PSA");
    indexNodeMap.put("jaxa-registry", "JAXA");
    indexNodeMap.put("dev-registry", "PDS_ENG_DEV");
  }
  static public AccessRights exchange (HarvestConfigurationType cfg) {
    return AccessRights.valueOf(cfg.accessRights.name().replace("-", "_"));
  }
    static public List<CompressionPattern> exchange (CompressedType compressed) {
      ArrayList<CompressionPattern> result = new ArrayList<CompressionPattern>();
      if (compressed.files != null) {
        for (CompressedFilesType cf : compressed.files) {
          result.add(new CompressionPattern(cf.pattern, cf.algorithms.name(), cf.extnsion));
        }
      }
      return result;
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
      if (!indexNodeMap.containsKey (indexName)) {
        throw new IllegalArgumentException("Index \"" + indexName + "\" is not one of the supported indices (\"" + String.join("\", \"", indexNodeMap.keySet()) + "\"): use a supported index name in your configuration or request that index \"" + indexName + "\" be added to harvest by submitting a ticket on https://github.com/NASA-PDS/harvest/issues");
      }

      return indexNodeMap.get(indexName);
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
      if (result.getCompressed() == null) {
        result.setCompressed(forge.createCompressedType());
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
