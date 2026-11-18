package gov.nasa.pds.harvest.dao;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.exception.InvalidPDS4ProductException;


/**
 * Waits until documents that were just written can be found by a search.
 *
 * <p>Bulk writes ask the registry to refresh the index before they return, except on OpenSearch
 * Serverless, which does not support that and makes documents searchable some time after they
 * are written. There is no refresh call to make there either, so this class polls with the same
 * search Harvest uses to find registered products.
 */
public class SearchableWait {
  /**
   * Finds which of the given ids cannot be found by a search yet.
   */
  @FunctionalInterface
  public interface IdLookup {
    Set<String> nonExistingIds(Collection<String> ids) throws IOException, InvalidPDS4ProductException;
  }

  /**
   * Pause between two searches.
   */
  @FunctionalInterface
  public interface Pause {
    void sleep(long millis) throws InterruptedException;
  }

  public static final long DEFAULT_TIMEOUT_MILLIS = 300_000L;
  public static final long DEFAULT_POLL_MILLIS = 5_000L;

  private final Logger log = LogManager.getLogger(this.getClass());
  private final IdLookup lookup;
  private final long timeoutMillis;
  private final long pollMillis;
  private final Pause pause;


  public SearchableWait(IdLookup lookup) {
    this(lookup, DEFAULT_TIMEOUT_MILLIS, DEFAULT_POLL_MILLIS, Thread::sleep);
  }


  public SearchableWait(IdLookup lookup, long timeoutMillis, long pollMillis, Pause pause) {
    if (pollMillis <= 0) throw new IllegalArgumentException("pollMillis must be > 0");
    this.lookup = lookup;
    this.timeoutMillis = timeoutMillis;
    this.pollMillis = pollMillis;
    this.pause = pause;
  }


  /**
   * Wait until all ids can be found by a search.
   *
   * @param ids ids (LIDVIDs) of documents that were just written
   * @return true if all ids were found, false if some were still not found after the timeout,
   *         if the lookup rejects the ids (the registry cannot answer the check), or if the thread
   *         was interrupted
   */
  public boolean await(Collection<String> ids) {
    if (ids == null || ids.isEmpty()) return true;

    long waited = 0;
    while (true) {
      Set<String> missing = null;
      try {
        missing = lookup.nonExistingIds(ids);
      } catch (IOException ex) {
        // The registry may be briefly unreachable. Try again until the timeout.
        log.warn("Could not check if written products are searchable: " + ex.getMessage());
      } catch (InvalidPDS4ProductException ex) {
        log.warn("Cannot check if written products are searchable: " + ex.getMessage());
        return false;
      }
      if (missing != null && missing.isEmpty()) return true;

      if (waited >= timeoutMillis) {
        log.warn("Still waiting for " + (missing == null ? "unknown number of" : missing.size())
            + " of " + ids.size() + " written product(s) to become searchable after "
            + (timeoutMillis / 1000) + " s");
        return false;
      }

      log.info("Waiting for written products to become searchable ("
          + (missing == null ? "?" : missing.size()) + " of " + ids.size() + " not yet found)");
      try {
        pause.sleep(pollMillis);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        return false;
      }
      waited += pollMillis;
    }
  }
}
