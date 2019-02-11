// Copyright 2006-2010, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id$
package gov.nasa.pds.harvest.search.registry;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import gov.nasa.pds.registry.model.Association;
import gov.nasa.pds.registry.model.ExtrinsicObject;
import gov.nasa.pds.registry.provider.JAXBContextResolver;
import gov.nasa.pds.registry.provider.JacksonObjectMapperProvider;
import gov.nasa.pds.registry.query.AssociationFilter;
import gov.nasa.pds.registry.query.ExtrinsicFilter;
import gov.nasa.pds.registry.query.ObjectFilter;
import gov.nasa.pds.registry.query.RegistryQuery;

public class RegistryClient {
  private WebResource registryResource;
  private String user;
  private String password;
  private String mediaType;

  public RegistryClient(String baseUrl) throws RegistryClientException {
    this(baseUrl, null, null);
  }

  public RegistryClient(String baseUrl, String user, String password)
  throws RegistryClientException {
    ClientConfig clientConfig = new DefaultClientConfig();
    clientConfig.getClasses().add(JacksonObjectMapperProvider.class);
    clientConfig.getClasses().add(JAXBContextResolver.class);
    //With the current setup of the Security Service, we need to set up
    //an insecure SSL connection.
    HostnameVerifier hv = getHostnameVerifier();
    try {
      SSLContext ctx = this.getSSLContext();
      clientConfig.getProperties().put(
          HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
          new HTTPSProperties(hv, ctx));
    } catch (Exception e) {
      throw new RegistryClientException("Error occurred while initializing "
          + "the registry client: " + e.getMessage());
    }
    registryResource = Client.create(clientConfig).resource(baseUrl).path(
        "registry");
    if (user != null && password != null) {
      registryResource.addFilter(new HTTPBasicAuthFilter(user, password));
    }
    this.user = user;
    this.password = password;
    mediaType = MediaType.APPLICATION_XML;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  /**
   * Class that supports insecure SSL connection.
   *
   * @return HostnameVerifier object
   */
  private HostnameVerifier getHostnameVerifier() {
    HostnameVerifier hv = new HostnameVerifier() {

      @Override
      public boolean verify(String hostname, SSLSession session) {
        // TODO Auto-generated method stub
        return true;
      }
    };
    return hv;
  }

  /**
   * Class that supports insecure SSL connection. This method basically
   * says trust anything!
   *
   * @return an instance of the SSLContext.
   *
   * @throws KeyManagementException
   * @throws NoSuchAlgorithmException
   */
  private SSLContext getSSLContext() throws KeyManagementException,
  NoSuchAlgorithmException {
    final SSLContext sslContext = SSLContext.getInstance("SSL");

    sslContext.init(null, new TrustManager[] { new X509TrustManager() {
      @Override
      public void checkClientTrusted(
          X509Certificate[] chain, String authType)
          throws CertificateException {
        // TODO Auto-generated method stub

      }

      @Override
      public void checkServerTrusted(
          X509Certificate[] chain, String authType)
          throws CertificateException {
        // TODO Auto-generated method stub

      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        return null;
      }
    }}, new SecureRandom());

    return sslContext;
  }

  public ClientResponse publishExtrinsic(String user, ExtrinsicObject extrinsic) {
    WebResource.Builder builder = registryResource.path("extrinsics")
        .getRequestBuilder();
    return builder.accept(mediaType).post(ClientResponse.class, extrinsic);
  }

  public ClientResponse versionExtrinsic(String user,
      ExtrinsicObject extrinsic, String lid) {
    return this.versionExtrinsic(user, extrinsic, lid, true);
  }

  public ClientResponse versionExtrinsic(String user,
      ExtrinsicObject extrinsic, String lid, Boolean major) {
    WebResource.Builder builder = registryResource.path("extrinsics").path(
        extrinsic.getLid()).queryParam("major", major.toString())
        .getRequestBuilder();
    return builder.accept(mediaType).post(ClientResponse.class, extrinsic);
  }

  public ClientResponse getLatestExtrinsic(String lid) {
    WebResource.Builder builder = registryResource.path("extrinsics").path(lid)
        .getRequestBuilder();
    return builder.accept(mediaType).get(ClientResponse.class);
  }

  public ClientResponse getExtrinsic(String lid, String version) {
    WebResource.Builder builder = registryResource.path("extrinsics").path(lid)
        .path(version).getRequestBuilder();
    return builder.accept(mediaType).get(ClientResponse.class);
  }

  public ClientResponse getStatus() {
    WebResource.Builder builder = registryResource.path("status")
        .getRequestBuilder();
    return builder.accept(mediaType).get(ClientResponse.class);
  }

  public ClientResponse publishAssociation(String user, Association association) {
    WebResource.Builder builder = registryResource.path("associations")
        .getRequestBuilder();
    return builder.accept(mediaType).post(ClientResponse.class, association);
  }

  public ClientResponse getExtrinsics(RegistryQuery<ExtrinsicFilter> query,
      Integer start, Integer rows) {
    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    if (start != null) {
      params.add("start", start.toString());
    }
    if (rows != null) {
      params.add("rows", rows.toString());
    }

    ObjectFilter filter = query.getFilter();
    if (filter != null) {
      if (filter.getGuid() != null) {
        params.add("guid", filter.getGuid());
      }
      if (filter.getLid() != null) {
        params.add("lid", filter.getLid());
      }
      if (filter.getName() != null) {
        params.add("name", filter.getName());
      }
      if (filter.getObjectType() != null) {
        params.add("objectType", filter.getObjectType());
      }
      if (filter.getStatus() != null) {
        params.add("status", filter.getStatus().toString());
      }
      if (filter.getVersionName() != null) {
        params.add("versionName", filter.getVersionName());
      }
    }

    List<String> sort = query.getSort();
    for (String s : sort) {
      params.add("sort", s);
    }

    params.add("queryOp", query.getOperator().toString());

    WebResource.Builder builder = registryResource.path("extrinsics")
        .queryParams(params).getRequestBuilder();

    return builder.accept(mediaType).get(ClientResponse.class);
  }

  public ClientResponse getAssociations(RegistryQuery<AssociationFilter> query,
      Integer start, Integer rows) {
    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    if (start != null) {
      params.add("start", start.toString());
    }
    if (rows != null) {
      params.add("rows", rows.toString());
    }

    AssociationFilter filter = query.getFilter();
    if (filter != null) {
      if (filter.getTargetObject() != null) {
        params.add("targetObject", filter.getTargetObject());
      }
      if (filter.getSourceObject() != null) {
        params.add("sourceObject", filter.getSourceObject());
      }
      if (filter.getAssociationType() != null) {
        params.add("associationType", filter.getAssociationType());
      }
    }

    List<String> sort = query.getSort();
    for (String s : sort) {
      params.add("sort", s);
    }

    params.add("queryOp", query.getOperator().toString());

    WebResource.Builder builder = registryResource.path("associations")
        .queryParams(params).getRequestBuilder();

    return builder.accept(mediaType).get(ClientResponse.class);
  }
}
