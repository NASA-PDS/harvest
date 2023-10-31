package harvest.util.xml;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import gov.nasa.pds.harvest.util.xml.XmlIs;

class XmlIsSuite {
  @Test
  void testBundle() {
    assertTrue(XmlIs.aBundle ("src/test/resources/test_data/sample_dossier.lblx"));
    assertFalse(XmlIs.aBundle ("src/test/resources/test_data/document/collection_document.lblx"));
    assertFalse(XmlIs.aBundle ("src/test/resources/test_data/document/sample_dossier_release_notes.lblx"));
  }
  @Test
  void testCollection() {
    assertFalse(XmlIs.aCollection ("src/test/resources/test_data/sample_dossier.lblx"));
    assertTrue(XmlIs.aCollection ("src/test/resources/test_data/document/collection_document.lblx"));
    assertFalse(XmlIs.aCollection ("src/test/resources/test_data/document/sample_dossier_release_notes.lblx"));
  }
  @Test
  void testLabel() {
    assertTrue(XmlIs.aLabel ("src/test/resources/test_data/sample_dossier.lblx"));
    assertTrue(XmlIs.aLabel ("src/test/resources/test_data/document/collection_document.lblx"));
    assertTrue(XmlIs.aLabel ("src/test/resources/test_data/document/sample_dossier_release_notes.lblx"));
  }
  static public int main(String[] args) {
    new XmlIsSuite().testBundle();
    return 0;
  }
}
