package gov.nasa.pds.harvest.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import gov.nasa.pds.harvest.LogCapture;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.dao.SearchableWait;
import gov.nasa.pds.harvest.exception.HarvestException;
import gov.nasa.pds.harvest.util.out.WriterManager;


/**
 * Runs the Harvest command against an in-memory registry and checks the order in which
 * documents are written: products, then bundles, then collections with their registry-refs.
 * The order is a best effort: failed product files and a wait for searchable products that times
 * out do not hold back the bundles and collections.
 */
class HarvestOrderTest {
  private static final String BUNDLE = "urn:test:b::1.0";
  private static final String COLLECTION = "urn:test:b:c::1.0";
  private static final String P1 = "urn:test:b:c:p1::1.0";
  private static final String P2 = "urn:test:b:c:p2::1.0";

  @TempDir
  Path dir;

  private FakeRegistry registry;
  private Path root;
  private Path authFile;


  @BeforeEach
  void setUp() throws IOException {
    registry = new FakeRegistry();
    root = Files.createDirectories(dir.resolve("archive"));
    // Harvest requires an auth file even though this test never connects to a registry
    authFile = write(dir.resolve("auth.cfg"), "user=unused\n" + "pass" + "word=unused\n");

    write(root.resolve("bundle.xml"), bundleLabel());
    write(root.resolve("c/collection.xml"), collectionLabel());
    write(root.resolve("c/inventory.csv"), "P," + P1 + "\nP," + P2 + "\n");
    write(root.resolve("c/p1.xml"), productLabel("urn:test:b:c:p1"));
    write(root.resolve("c/p2.xml"), productLabel("urn:test:b:c:p2"));
  }


  @AfterEach
  void tearDown() throws ReflectiveOperationException {
    RegistryManager.destroy();
    resetWriterManager();
  }


  // Directory, file list and bundle modes

  @Test
  void fileListWithAggregatesFirstWritesProductsBundlesThenCollections() throws Exception {
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    harvest(false, config(files(manifest)));

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  @Test
  void directoryWritesProductsBundlesThenCollections() throws Exception {
    harvest(false, config(directories(root)));

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  @Test
  void bundleModeWritesProductsBundlesThenCollections() throws Exception {
    harvest(false, config("<bundles><bundle dir=\"" + root + "\" versions=\"all\"/></bundles>"));

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  @Test
  void overwriteWritesTheSameDocumentsInTheSameOrder() throws Exception {
    registry.registered.addAll(Arrays.asList(BUNDLE, COLLECTION, P1, P2));
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    harvest(true, config(files(manifest)));

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  @Test
  void overwriteInBundleModeWritesTheSameDocumentsInTheSameOrder() throws Exception {
    registry.registered.addAll(Arrays.asList(BUNDLE, COLLECTION, P1, P2));

    harvest(true, config("<bundles><bundle dir=\"" + root + "\" versions=\"all\"/></bundles>"));

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  @Test
  void registeredProductsAreSkippedWithoutOverwrite() throws Exception {
    registry.registered.addAll(Arrays.asList(P1, P2));
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    harvest(false, config(files(manifest)));

    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "refs " + COLLECTION),
        sortedDocs());
  }


  @Test
  void productsOnlyBehavesAsBefore() throws Exception {
    Path manifest = manifest("c/p1.xml", "c/p2.xml");

    harvest(false, config(files(manifest)));

    assertEquals(Arrays.asList("product " + P1, "product " + P2), registry.writtenDocs());
    assertTrue(!registry.events.contains("searchable"), "nothing to wait for before aggregates");
  }


  @Test
  void bundlesAndCollectionsOnlyBehavesAsBefore() throws Exception {
    Path manifest = manifest("c/collection.xml", "bundle.xml");

    harvest(false, config(files(manifest)));

    // Bundle first, then the collection together with its refs
    assertEquals(Arrays.asList("bundle " + BUNDLE, "refs " + COLLECTION, "collection " + COLLECTION),
        registry.events.stream().filter(e -> !e.equals("searchable")).collect(java.util.stream.Collectors.toList()));
  }


  @Test
  void waitsUntilProductsAreSearchableBeforeAggregates() throws Exception {
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    harvest(false, config(files(manifest)));

    int searchable = registry.events.indexOf("searchable");
    assertTrue(searchable > registry.events.indexOf("product " + P2), registry.events.toString());
    assertTrue(searchable < registry.events.indexOf("bundle " + BUNDLE), registry.events.toString());
    assertTrue(searchable < registry.events.indexOf("refs " + COLLECTION), registry.events.toString());
  }


  // Best effort: nothing that Harvest wrote before is held back

  @Test
  void failedProductStillWritesBundleAndCollectionAfterTheProductsAndExitsNonZero() throws Exception {
    write(root.resolve("c/p2.xml"), "<Product_Document>not xml");
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    HarvestException failure = assertThrows(HarvestException.class, () -> harvest(false, config(files(manifest))));

    assertTrue(failure.getMessage().contains("1 file(s) failed to load"), failure.getMessage());
    assertEquals(Arrays.asList("product " + P1, "bundle " + BUNDLE, "refs " + COLLECTION,
        "collection " + COLLECTION), withoutSearchable());
  }


  @Test
  void waitTimeoutLogsWarningAndStillWritesBundleAndCollectionAfterTheProducts() throws Exception {
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");
    SearchableWait never = new SearchableWait(ids -> new HashSet<>(ids), 3, 1, millis -> {});

    try (LogCapture log = new LogCapture()) {
      harvest(false, config(files(manifest)), never);

      assertTrue(log.warnings().stream().anyMatch(w -> w.contains("searchable")), log.warnings().toString());
      assertEquals(Collections.emptyList(), log.errors());
    }

    assertAggregatesLast();
    assertEquals(Arrays.asList("bundle " + BUNDLE, "collection " + COLLECTION, "product " + P1,
        "product " + P2, "refs " + COLLECTION), sortedDocs());
  }


  // An exception that aborts the run before the aggregates stage: nothing more can run

  @Test
  void abortByExceptionWritesNoBundleOrCollectionAndPropagatesTheError() throws Exception {
    registry.failProductWrites = true;
    Path manifest = manifest("c/collection.xml", "bundle.xml", "c/p1.xml", "c/p2.xml");

    assertThrows(IOException.class, () -> harvest(false, config(files(manifest))));

    assertEquals(Collections.emptyList(), registry.events);
  }


  // Helpers

  /** Products first, then the bundle, then the collection and its refs. */
  private void assertAggregatesLast() {
    List<String> events = registry.events;
    int lastProduct = Math.max(events.lastIndexOf("product " + P1), events.lastIndexOf("product " + P2));
    int bundle = events.indexOf("bundle " + BUNDLE);
    int refs = events.indexOf("refs " + COLLECTION);
    int collection = events.indexOf("collection " + COLLECTION);

    assertTrue(lastProduct >= 0 && bundle >= 0 && refs >= 0 && collection >= 0, events.toString());
    assertTrue(lastProduct < bundle, "products before the bundle: " + events);
    assertTrue(bundle < refs, "bundle before the collection refs: " + events);
    assertTrue(bundle < collection, "bundle before the collection: " + events);
  }


  private List<String> sortedDocs() {
    List<String> docs = withoutSearchable();
    Collections.sort(docs);
    return docs;
  }


  private List<String> withoutSearchable() {
    List<String> docs = new ArrayList<>(registry.events);
    docs.removeAll(Collections.singleton("searchable"));
    return docs;
  }


  private void harvest(boolean overwrite, Path config) throws Exception {
    harvest(overwrite, config, new SearchableWait(ids -> {
      registry.events.add("searchable");
      return new HashSet<>();
    }));
  }


  /** WriterManager.destroy() closes the writers but keeps the singleton, so a second run in the same JVM fails. */
  private static void resetWriterManager() throws ReflectiveOperationException {
    WriterManager.destroy();
    Field instance = WriterManager.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }


  private void harvest(boolean overwrite, Path config, SearchableWait wait) throws Exception {
    resetWriterManager();
    RegistryManager.init(registry.connectionFactory(), overwrite, false);
    RegistryManager.getInstance().getRegistryWriter().setSearchableWait(wait);

    Options options = new Options();
    options.addOption(Option.builder("c").hasArg().build());
    options.addOption(Option.builder("o").hasArg().build());
    options.addOption(Option.builder("O").longOpt("overwrite").build());

    List<String> args = new ArrayList<>(Arrays.asList("-c", config.toString(), "-o", dir.resolve("out").toString()));
    if (overwrite) args.add("-O");
    CommandLine cmdLine = new DefaultParser().parse(options, args.toArray(new String[0]));

    new HarvestCmd().run(cmdLine);
  }


  private Path config(String load) throws IOException {
    return write(dir.resolve("harvest.xml"), "<harvest>"
        + "<registry auth=\"" + authFile + "\">app://connections/direct/localhost.xml</registry>"
        + "<load>" + load + "</load>"
        + "<fileInfo processDataFiles=\"false\" storeLabels=\"false\" storeJsonLabels=\"false\"/>"
        + "<autogenFields/>"
        + "</harvest>");
  }


  private static String directories(Path path) {
    return "<directories><path>" + path + "</path></directories>";
  }


  private static String files(Path manifest) {
    return "<files><manifest>" + manifest + "</manifest></files>";
  }


  private Path manifest(String... labels) throws IOException {
    StringBuilder lines = new StringBuilder();
    for (String label : labels) {
      lines.append(root.resolve(label)).append('\n');
    }
    return write(dir.resolve("manifest.txt"), lines.toString());
  }


  private static Path write(Path path, String content) throws IOException {
    Files.createDirectories(path.getParent());
    return Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }


  private static String label(String rootElement, String lid, String productClass, String body) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<" + rootElement + " xmlns=\"http://pds.nasa.gov/pds4/pds/v1\">"
        + "<Identification_Area><logical_identifier>" + lid + "</logical_identifier>"
        + "<version_id>1.0</version_id><title>" + lid + "</title>"
        + "<product_class>" + productClass + "</product_class></Identification_Area>"
        + body
        + "</" + rootElement + ">";
  }


  private static String productLabel(String lid) {
    return label("Product_Document", lid, "Product_Document", "");
  }


  private static String bundleLabel() {
    return label("Product_Bundle", "urn:test:b", "Product_Bundle",
        "<Bundle_Member_Entry><lidvid_reference>" + COLLECTION + "</lidvid_reference>"
            + "<member_status>Primary</member_status>"
            + "<reference_type>bundle_has_data_collection</reference_type></Bundle_Member_Entry>");
  }


  private static String collectionLabel() {
    return label("Product_Collection", "urn:test:b:c", "Product_Collection",
        "<Collection><collection_type>Data</collection_type></Collection>"
            + "<File_Area_Inventory><File><file_name>inventory.csv</file_name></File>"
            + "<Inventory><offset unit=\"byte\">0</offset><records>2</records>"
            + "<reference_type>inventory_has_member_product</reference_type></Inventory>"
            + "</File_Area_Inventory>");
  }
}
