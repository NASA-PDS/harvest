package gov.nasa.pds.harvest.cmd;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;

import gov.nasa.pds.registry.common.ConnectionFactory;
import gov.nasa.pds.registry.common.Request;
import gov.nasa.pds.registry.common.Response;
import gov.nasa.pds.registry.common.RestClient;


/**
 * An in-memory stand-in for the registry. It records every document that Harvest writes, in the
 * order it is written, and answers the "which of these ids exist" searches from what was written.
 * No connection is made.
 */
class FakeRegistry {
  static final String INDEX = "dev-registry";

  private static final Pattern ID = Pattern.compile("\"_id\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern PRODUCT_CLASS = Pattern.compile("\"product_class\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern COLLECTION_LIDVID = Pattern.compile("\"collection_lidvid\"\\s*:\\s*\"([^\"]+)\"");

  /**
   * Everything written, in order: "product ID", "bundle ID", "collection ID", "refs COLLECTION_ID".
   * Other events can be added by tests.
   */
  final List<String> events = Collections.synchronizedList(new ArrayList<>());

  /** LIDVIDs of documents in the registry index. */
  final Set<String> registered = Collections.synchronizedSet(new HashSet<>());

  /** When true, writing to the registry index fails. */
  volatile boolean failProductWrites = false;

  boolean serverless = false;


  ConnectionFactory connectionFactory() {
    return new Connection(INDEX);
  }


  /** Documents written to the registry index, in order, without the refs documents. */
  List<String> writtenDocs() {
    List<String> docs = new ArrayList<>();
    synchronized (events) {
      for (String event : events) {
        if (event.startsWith("product ") || event.startsWith("bundle ") || event.startsWith("collection ")) {
          docs.add(event);
        }
      }
    }
    return docs;
  }


  private class Connection implements ConnectionFactory {
    private String indexName;

    Connection(String indexName) {
      this.indexName = indexName;
    }

    @Override
    public ConnectionFactory clone() {
      return new Connection(indexName);
    }

    @Override
    public RestClient createRestClient() {
      return client();
    }

    @Override
    public CredentialsProvider getCredentials() {
      return null;
    }

    @Override
    public org.apache.hc.client5.http.auth.CredentialsProvider getCredentials5() {
      return null;
    }

    @Override
    public HttpHost getHost() {
      return null;
    }

    @Override
    public org.apache.hc.core5.http.HttpHost getHost5() {
      return null;
    }

    @Override
    public String getHostName() {
      return "fake";
    }

    @Override
    public String getIndexName() {
      return indexName;
    }

    @Override
    public boolean isTrustingSelfSigned() {
      return false;
    }

    @Override
    public void reconnect() {}

    @Override
    public ConnectionFactory setIndexName(String idxName) {
      this.indexName = idxName;
      return this;
    }

    @Override
    public boolean isServerless() {
      return serverless;
    }

    @Override
    public String toString() {
      return "fake registry";
    }
  }


  private static class Bulk implements Request.Bulk {
    String index;
    final List<String> statements = new ArrayList<>();
    final List<String> documents = new ArrayList<>();

    @Override
    public void add(String statement, String document) {
      statements.add(statement);
      documents.add(document);
    }

    @Override
    public Request.Bulk buildUpdateStatus(Collection<String> lidvids, String status) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Request.Bulk setIndex(String name) {
      this.index = name;
      return this;
    }

    @Override
    public Request.Bulk setRefresh(Refresh type) {
      return this;
    }
  }


  private static class SearchCall implements InvocationHandler {
    Collection<String> ids = Collections.emptyList();

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
      if (method.getName().equals("buildTheseIds")) {
        ids = (Collection<String>) args[0];
      }
      if (method.getReturnType() == Request.Search.class) return proxy;
      return null;
    }
  }


  private RestClient client() {
    return (RestClient) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {RestClient.class},
        (proxy, method, args) -> {
          switch (method.getName()) {
            case "createBulkRequest":
              return new Bulk();
            case "createSearchRequest":
              return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Request.Search.class},
                  new SearchCall());
            case "createMappingRequest":
              return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Request.Mapping.class},
                  (p, m, a) -> m.getReturnType() == Request.Mapping.class ? p : null);
            case "exists":
              return true;
            case "close":
              return null;
            case "performRequest":
              return perform(args[0]);
            default:
              throw new UnsupportedOperationException(method.getName());
          }
        });
  }


  private Object perform(Object request) throws IOException {
    if (request instanceof Bulk) {
      return write((Bulk) request);
    }
    if (request instanceof Request.Mapping) {
      return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Response.Mapping.class},
          (p, m, a) -> m.getName().equals("fieldNames") ? everything() : null);
    }
    SearchCall search = (SearchCall) Proxy.getInvocationHandler(request);
    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Response.Search.class},
        (p, m, a) -> {
          if (m.getName().equals("nonExistingIds")) {
            Set<String> missing = new HashSet<>(search.ids);
            synchronized (registered) {
              missing.removeAll(registered);
            }
            return missing;
          }
          if (m.getName().equals("fields")) return new HashSet<String>();
          return null;
        });
  }


  /** A set that says it contains every field, so that no schema or LDD update is needed. */
  private static Set<String> everything() {
    return new HashSet<String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean contains(Object o) {
        return true;
      }
    };
  }


  private Response.Bulk write(Bulk bulk) throws IOException {
    boolean registryIndex = INDEX.equals(bulk.index);
    if (registryIndex && failProductWrites) {
      throw new IOException("simulated registry failure");
    }

    for (int i = 0; i < bulk.statements.size(); i++) {
      String id = find(ID, bulk.statements.get(i));
      String doc = bulk.documents.get(i);
      if (registryIndex) {
        String productClass = find(PRODUCT_CLASS, doc);
        String kind = "Product_Bundle".equals(productClass) ? "bundle"
            : "Product_Collection".equals(productClass) ? "collection" : "product";
        events.add(kind + " " + id);
        registered.add(id);
      } else {
        events.add("refs " + find(COLLECTION_LIDVID, doc));
      }
    }

    return (Response.Bulk) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Response.Bulk.class},
        (p, m, a) -> {
          switch (m.getName()) {
            case "errors":
              return false;
            case "items":
              return Collections.emptyList();
            case "took":
              return 0L;
            default:
              return null;
          }
        });
  }


  private static String find(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find()) throw new IllegalStateException("No " + pattern + " in " + text);
    return matcher.group(1);
  }
}
