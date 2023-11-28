package gov.nasa.pds.harvest.util.xml;

import java.io.FileInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.apache.commons.io.input.BOMInputStream;
import org.xml.sax.InputSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.xpath.XPathEvaluator;

public class XmlIs {
  private static String ANY_TYPE = "logical_identifier";
  private static boolean interogate(String filename, String type) {
    if (filename.endsWith (".xml") || filename.endsWith (".lblx")) {
      try {
        XPathEvaluator xpath = new XPathEvaluator();
        Configuration configuration = xpath.getConfiguration();
        configuration.setLineNumbering(true);
        configuration.setXIncludeAware(false);
        ParseOptions options = new ParseOptions();
        options.withLineNumbering(true);
        options.withXIncludeAware(false);
        Source source = new SAXSource(new InputSource(new BOMInputStream(new FileInputStream(filename))));
        TreeInfo docInfo = configuration.buildDocumentTree(source , options);
        NodeInfo ia=null,lid=null,pcls=null;
        for (NodeInfo top : docInfo.getRootNode().children()) {
          for (NodeInfo child : top.children()) {
            if ("Identification_Area".equals(child.getLocalPart())) {
              if (ia == null) {
                ia = child;
              } else {
                throw new RuntimeException("Cannot have more than one <Identification_Area> in a valid PDS4 XML file");
              }
            }
          }
        }
        for (NodeInfo child : ia.children()) {
          if ("logical_identifier".equals(child.getLocalPart())) {
            if (lid == null) {
              lid = child;
            } else {
              throw new RuntimeException("Cannot have more than one <logical_identifier> in the <Identification_Area> in a valid PDS4 XML file");
            }
          }
          if ("product_class".equals(child.getLocalPart())) {
            if (pcls == null) {
              pcls = child;
            } else { 
              throw new RuntimeException("Cannot have more than one <product_class> in the <Identification_Area> in a valid PDS4 XML file");
            }
          }
        }
        return type == ANY_TYPE ? true : type.equals(pcls.getStringValue());
      } catch (Exception e) { /* ignore it because it just means not a Label */ }
    }
    return false;
  }
  public static boolean aBundle (String filename) {
    return interogate(filename, "Product_Bundle");
  }
  public static boolean aCollection (String filename) {
    return interogate (filename, "Product_Collection");
  }
  public static boolean aLabel (String filename) {
    return interogate(filename, ANY_TYPE);
  }
}
