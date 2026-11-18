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

import gov.nasa.pds.harvest.LogCapture;


class AggregatesLastTest {
  private final List<String> events = new ArrayList<>();
  private IOException flushFailure = null;
  private boolean searchable = true;

  private final AggregatesLast.ProductWriter writer = new AggregatesLast.ProductWriter() {
    @Override
    public void flush() throws IOException {
      events.add("flush");
      if (flushFailure != null) throw flushFailure;
    }

    @Override
    public boolean awaitSearchable() {
      events.add("searchable");
      return searchable;
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
  void warnsAndStillWritesBundlesAndCollectionsWhenProductsCannotBeConfirmedSearchable() throws Exception {
    searchable = false;

    try (LogCapture log = new LogCapture()) {
      assertTrue(AggregatesLast.write(aggregates(), writer));

      assertEquals(1, log.warnings().size(), log.warnings().toString());
      assertTrue(log.warnings().get(0).contains("searchable"), log.warnings().get(0));
      assertEquals(new ArrayList<String>(), log.errors());
    }

    assertEquals(Arrays.asList("flush", "searchable", "bundle 1", "bundle 2", "flush", "collection 1",
        "collection 2", "flush"), events);
  }


  @Test
  void doesNotWarnWhenProductsAreSearchable() throws Exception {
    try (LogCapture log = new LogCapture()) {
      AggregatesLast.write(aggregates(), writer);

      assertEquals(new ArrayList<String>(), log.warnings());
    }
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
