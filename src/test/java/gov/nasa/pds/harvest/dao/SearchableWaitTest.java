package gov.nasa.pds.harvest.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;


class SearchableWaitTest {
  private static final List<String> IDS = Arrays.asList("a::1.0", "b::1.0");

  private final AtomicInteger pauses = new AtomicInteger();


  private SearchableWait wait(SearchableWait.IdLookup lookup, long timeoutMillis) {
    return new SearchableWait(lookup, timeoutMillis, 1000, millis -> pauses.incrementAndGet());
  }


  @Test
  void returnsAtOnceWhenEverythingIsAlreadySearchable() throws Exception {
    assertTrue(wait(ids -> Collections.emptySet(), 10_000).await(IDS));

    assertEquals(0, pauses.get());
  }


  @Test
  void returnsAtOnceWhenNothingWasWritten() throws Exception {
    assertTrue(wait(ids -> {
      throw new IOException("must not search");
    }, 10_000).await(Collections.emptyList()));
  }


  @Test
  void waitsUntilTheDocumentsAppear() throws Exception {
    AtomicInteger searches = new AtomicInteger();

    boolean found = wait(ids -> searches.incrementAndGet() < 3 ? new HashSet<>(ids) : Collections.emptySet(), 10_000)
        .await(IDS);

    assertTrue(found);
    assertEquals(3, searches.get());
    assertEquals(2, pauses.get());
  }


  @Test
  void givesUpAfterTheTimeout() throws Exception {
    Set<String> missing = new HashSet<>(Arrays.asList("b::1.0"));

    assertFalse(wait(ids -> missing, 3000).await(IDS));

    assertEquals(3, pauses.get());
  }


  @Test
  void keepsTryingWhenTheRegistryIsBrieflyUnavailable() throws Exception {
    AtomicInteger searches = new AtomicInteger();

    boolean found = wait(ids -> {
      if (searches.incrementAndGet() == 1) throw new IOException("connection reset");
      return Collections.emptySet();
    }, 10_000).await(IDS);

    assertTrue(found);
    assertEquals(2, searches.get());
  }


  @Test
  void stopsWhenInterrupted() throws Exception {
    SearchableWait wait = new SearchableWait(ids -> new HashSet<>(ids), 10_000, 1000, millis -> {
      throw new InterruptedException();
    });

    assertFalse(wait.await(IDS));
    assertTrue(Thread.interrupted(), "interrupt flag is kept");
  }
}
