package gov.nasa.pds.harvest.crawler;

import java.io.File;
import java.util.logging.Logger;

import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionRepo;
import gov.nasa.pds.harvest.constants.Constants;
import gov.nasa.pds.harvest.crawler.metadata.extractor.Pds4MetExtractorConfig;
import gov.nasa.pds.harvest.inventory.InventoryEntry;
import gov.nasa.pds.harvest.inventory.InventoryReader;
import gov.nasa.pds.harvest.inventory.InventoryReaderException;
import gov.nasa.pds.harvest.inventory.InventoryTableReader;
import gov.nasa.pds.harvest.logging.ToolsLevel;
import gov.nasa.pds.harvest.logging.ToolsLogRecord;
import gov.nasa.pds.harvest.util.XMLExtractor;

/**
 * A crawler class for a PDS Collection file.
 *
 * @author mcayanan
 *
 */
public class CollectionCrawler extends PDSProductCrawler {
  /** Logger object. */
  private static Logger log = Logger.getLogger(
      CollectionCrawler.class.getName());

  /**
   * Constructor.
   *
   * @param extractorConfig A configuration class for the metadata
   * extractor.
   */
  public CollectionCrawler(Pds4MetExtractorConfig extractorConfig) {
    super(extractorConfig);
  }

  /**
   * Crawl a PDS4 collection file. Method will register the collection
   * first before attempting to register the product files it is pointing
   * to.
   *
   * @param collection The PDS4 Collection file.
   *
   */
  public void crawl(File collection) {
    //Load actions first before crawling
    CrawlerActionRepo repo = new CrawlerActionRepo();
    repo.loadActions(getActions());
    setActionRepo(repo);
    handleFile(collection);
    try {
      XMLExtractor extractor = new XMLExtractor();
      extractor.parse(collection);
      if ("".equals(extractor.getValueFromDoc(
          Constants.PRIMARY_COLLECTION_XPATH))) {
        if ("".equals(extractor.getValueFromDoc(
            Constants.SECONDARY_COLLECTION_XPATH))) {
          log.log(new ToolsLogRecord(ToolsLevel.WARNING, "Cannot determine if "
              + "this is a primary or secondary collection. Members will "
              + "not be registered.", collection));
          return;
        } else {
          log.log(new ToolsLogRecord(ToolsLevel.INFO, "Not a primary "
              + "collection. Members will not be registered.", collection));
          return;
        }
      } else {
        log.log(new ToolsLogRecord(ToolsLevel.INFO, "This is a primary "
            + "collection. Members will be registered.", collection));
      }
      InventoryReader reader = new InventoryTableReader(collection);
      for (InventoryEntry entry = new InventoryEntry(); entry != null;) {
        if (!entry.isEmpty()) {
          if (!entry.getChecksum().isEmpty()) {
            Constants.suppliedChecksums.put(entry.getFile(),
                entry.getChecksum());
          }
          handleFile(entry.getFile());
        }
        try {
          entry = reader.getNext();
        } catch (InventoryReaderException ir) {
          log.log(new ToolsLogRecord(ToolsLevel.SEVERE, ir.getMessage(),
            collection.toString(), ir.getLineNumber()));
        }
      }
    } catch (Exception e) {
      log.log(new ToolsLogRecord(ToolsLevel.SEVERE, e.getMessage(),
          collection));
    }
  }
}
