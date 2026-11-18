package gov.nasa.pds.harvest.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Writes the bundles and collections of a Harvest run after all other products of the run.
 *
 * <p>Order: products (already written by the crawl), flush and wait until the products are
 * searchable, bundles, collections (each collection together with its registry-refs documents).
 * Nothing is written when the run fails before this step, or when products of the run failed to
 * load, so that the incomplete load stays visible and a re-run completes it.
 */
public final class AggregatesLast {
  /**
   * The registry writer, as seen by this step.
   */
  public interface ProductWriter {
    /**
     * Write all buffered documents.
     */
    void flush() throws Exception;

    /**
     * Wait until the products written by this run can be found by a search.
     */
    void awaitSearchable() throws Exception;

    /**
     * Number of files of this run that failed to load so far.
     */
    int failedCount();
  }

  private static final Logger log = LogManager.getLogger(AggregatesLast.class);

  private AggregatesLast() {}


  /**
   * Write the queued bundles, then the queued collections.
   *
   * @param aggregates queued bundles and collections, emptied by this call
   * @param writer registry writer
   * @return true if the bundles and collections were written, false if there was nothing to write
   *         or they were not written because products failed to load
   * @throws Exception when flushing, waiting or writing a bundle or collection fails; bundles and
   *         collections that were not written yet stay unwritten
   */
  public static boolean write(DeferredAggregates aggregates, ProductWriter writer) throws Exception {
    if (aggregates.isEmpty()) return false;

    try {
      // Everything else of this run is in the registry before the first bundle or collection
      writer.flush();

      int failed = writer.failedCount();
      if (failed > 0) {
        log.error(failed + " file(s) failed to load. Not writing " + aggregates.bundleCount()
            + " bundle(s) and " + aggregates.collectionCount()
            + " collection(s) so that the incomplete load stays visible. "
            + "Fix the failures and run Harvest again.");
        return false;
      }

      writer.awaitSearchable();

      log.info("Writing " + aggregates.bundleCount() + " bundle(s)");
      for (DeferredAggregates.Task task : aggregates.bundleTasks()) {
        task.run();
      }
      writer.flush();

      log.info("Writing " + aggregates.collectionCount() + " collection(s)");
      for (DeferredAggregates.Task task : aggregates.collectionTasks()) {
        task.run();
      }
      writer.flush();
      return true;
    } finally {
      aggregates.clear();
    }
  }
}
