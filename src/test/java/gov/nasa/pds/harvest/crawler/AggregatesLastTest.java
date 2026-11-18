package gov.nasa.pds.harvest.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


class AggregatesLastTest {
  private final List<String> events = new ArrayList<>();
  private int failed = 0;
  private IOException flushFailure = null;
  private IOException searchableFailure = null;

  private final AggregatesLast.ProductWriter writer = new AggregatesLast.ProductWriter() {
    @Override
    public void flush() throws IOException {
      events.add("flush");
      if (flushFailure != null) throw flushFailure;
    }

    @Override
    public void awaitSearchable() throws IOException {
      events.add("searchable");
      if (searchableFailure != null) throw searchableFailure;
    }

    @Override
    public int failedCount() {
      return failed;
    }
  };


  private DeferredAggregates aggregates() {
    DeferredAggregates aggregates = new DeferredAggregates();
    // Queued in the order a crawl may meet them: collection, bundle, collection, bundle
    aggregates.deferCollection(() -> events.add("collection 1"));
    aggregates.deferBundle(() -> events.add("bundle 1"));
    aggregates.deferCollection(() -> events.add("collection 2"));
    aggregates.deferBundle(() -> events.add("bundle 2"));
    return aggregates;
  }


  @Test
  void writesProductsFlushWaitBundlesThenCollections() throws Exception {
    events.add("products written");

    assertTrue(AggregatesLast.write(aggregates(), writer));

    assertEquals(Arrays.asList("products written", "flush", "searchable", "bundle 1", "bundle 2", "flush",
        "collection 1", "collection 2", "flush"), events);
  }


  @Test
  void queueIsEmptiedAfterWriting() throws Exception {
    DeferredAggregates aggregates = aggregates();

    AggregatesLast.write(aggregates, writer);

    assertTrue(aggregates.isEmpty());
  }


  @Test
  void doesNothingWhenThereAreNoBundlesOrCollections() throws Exception {
    assertFalse(AggregatesLast.write(new DeferredAggregates(), writer));

    assertEquals(new ArrayList<String>(), events);
  }


  @Test
  void writesNothingWhenFlushingProductsFails() {
    flushFailure = new IOException("registry unavailable");
    DeferredAggregates aggregates = aggregates();

    assertThrows(IOException.class, () -> AggregatesLast.write(aggregates, writer));

    assertEquals(Arrays.asList("flush"), events);
    assertTrue(aggregates.isEmpty(), "a failed run must not leave work queued");
  }


  @Test
  void writesNothingWhenProductsDoNotBecomeSearchable() {
    searchableFailure = new IOException("not searchable");

    assertThrows(IOException.class, () -> AggregatesLast.write(aggregates(), writer));

    assertEquals(Arrays.asList("flush", "searchable"), events);
  }


  @Test
  void writesNothingWhenProductsFailedToLoad() throws Exception {
    failed = 1;

    assertFalse(AggregatesLast.write(aggregates(), writer));

    assertEquals(Arrays.asList("flush"), events);
  }


  @Test
  void stopsAtTheFirstFailedAggregate() {
    DeferredAggregates aggregates = new DeferredAggregates();
    aggregates.deferBundle(() -> {
      throw new IOException("bundle failed");
    });
    aggregates.deferCollection(() -> events.add("collection"));

    assertThrows(IOException.class, () -> AggregatesLast.write(aggregates, writer));

    assertEquals(Arrays.asList("flush", "searchable"), events);
  }
}
