package gov.nasa.pds.harvest.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Writes the bundles and collections of a Harvest run after all other products of the run.
 *
 * <p>Order: products (already written by the crawl), flush and wait until the products are
 * searchable, bundles, collections (each collection together with its registry-refs documents).
 *
 * <p>The order is a best effort that reduces reprocessing by the sweepers. It never holds back
 * a write: bundles and collections are written even if some product files failed to load, and
 * even if the products could not be confirmed searchable (a warning is logged). Only an
 * exception that aborts the run before this step prevents the bundles and collections from being
 * written.
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
     * Wait a bounded time until the products written by this run can be found by a search.
     *
     * @return true if the products were found, false if they could not be confirmed (timeout, or
     *         the registry does not support the check)
     */
    boolean awaitSearchable() throws Exception;
  }

  private static final Logger log = LogManager.getLogger(AggregatesLast.class);

  private AggregatesLast() {}


  /**
   * Write the queued bundles, then the queued collections.
   *
   * @param aggregates queued bundles and collections, emptied by this call
   * @param writer registry writer
   * @return true if the bundles and collections were written, false if there was nothing to write
   * @throws Exception when flushing or writing a product, a bundle or a collection fails; bundles
   *         and collections that were not written yet stay unwritten
   */
  public static boolean write(DeferredAggregates aggregates, ProductWriter writer) throws Exception {
    if (aggregates.isEmpty()) return false;

    try {
      // Everything else of this run is in the registry before the first bundle or collection
      writer.flush();

      // Best effort: not being able to confirm that the products are searchable does not stop the
      // bundles and collections from being written
      if (!writer.awaitSearchable()) {
        log.warn("Could not confirm that the written products are searchable. "
            + "Writing the bundles and collections anyway; the sweepers repair what is missed.");
      }

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
