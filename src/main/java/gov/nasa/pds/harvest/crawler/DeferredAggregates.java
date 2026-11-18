package gov.nasa.pds.harvest.crawler;

import java.util.ArrayList;
import java.util.List;


/**
 * Bundles and collections found during a Harvest run, queued to be written after all other
 * products of the run.
 *
 * <p>Only a small task per label is queued (it holds the label file, not the parsed label and
 * never the collection inventory). The label and its inventory are read again when the task
 * runs, so memory use does not depend on the size of the inventories.
 */
public class DeferredAggregates {
  /**
   * Work that writes one bundle or one collection (with its registry-refs documents).
   */
  @FunctionalInterface
  public interface Task {
    void run() throws Exception;
  }

  private final List<Task> bundles = new ArrayList<>();
  private final List<Task> collections = new ArrayList<>();


  public void deferBundle(Task task) {
    bundles.add(task);
  }


  public void deferCollection(Task task) {
    collections.add(task);
  }


  public boolean isEmpty() {
    return bundles.isEmpty() && collections.isEmpty();
  }


  public int bundleCount() {
    return bundles.size();
  }


  public int collectionCount() {
    return collections.size();
  }


  List<Task> bundleTasks() {
    return bundles;
  }


  List<Task> collectionTasks() {
    return collections;
  }


  /**
   * Forget all queued bundles and collections without writing them.
   */
  public void clear() {
    bundles.clear();
    collections.clear();
  }
}
