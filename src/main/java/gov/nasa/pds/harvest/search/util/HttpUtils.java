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
package gov.nasa.pds.harvest.search.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.NameValuePair;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class HttpUtils {
    public static ClientResponse post(String uri, Object requestEntity,
            MediaType contentType) {
        return post(uri, requestEntity, new ArrayList<NameValuePair>(),
                contentType, null);
    }

    public static ClientResponse post(String uri, NameValuePair parameter,
            MediaType contentType) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(parameter);

        return post(uri, params, contentType);
    }

    public static ClientResponse post(String uri, List<NameValuePair> parameters,
            MediaType contentType) {
        return post(uri, null, parameters, contentType, null);
    }

    public static ClientResponse post(String uri, Object requestEntity,
            MediaType contentType, String token) {
        return post(uri, requestEntity, new ArrayList<NameValuePair>(),
                contentType, token);
    }

    public static ClientResponse post(String uri, List<NameValuePair> parameters,
            MediaType contentType, String token) {
        return post(uri, null, parameters, contentType, token);
    }

    public static ClientResponse post(String uri, Object requestEntity,
            NameValuePair parameter, MediaType contentType) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(parameter);

        return post(uri, requestEntity, params, contentType, null);
    }


    public static ClientResponse post(String uri, Object requestEntity,
            NameValuePair parameter, MediaType contentType,
            String token) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(parameter);

        return post(uri, requestEntity, params, contentType, token);
    }

    public static ClientResponse post(String uri, Object requestEntity,
            List<NameValuePair> parameters, MediaType contentType,
            String token) {
        WebResource resource = Client.create().resource(uri);
        for(NameValuePair param : parameters) {
            resource = resource.queryParam(param.getName(), param.getValue());
        }
        WebResource.Builder builder = resource.getRequestBuilder();
        if(token != null) {
            builder = builder.header("Cookie", createCookie(token));
        }
        if(contentType != null)
            builder = builder.type(contentType);
        if(requestEntity != null) {
            builder = builder.entity(requestEntity);
        }
        return builder.post(ClientResponse.class);
    }

    public static ClientResponse get(String uri, MediaType contentType) {
        return get(uri, contentType, null);
    }

    public static ClientResponse get(String uri, MediaType contentType,
            String token) {
        WebResource resource = Client.create().resource(uri);
        WebResource.Builder builder = resource.getRequestBuilder();
        if(token != null) {
            builder = builder.header("Cookie", createCookie(token));
        }
        if(contentType != null)
            builder = builder.type(contentType);

        return builder.get(ClientResponse.class);
    }

    public static String createCookie(String token) {
        return "iPlanetDirectoryPro=\"" + token + "\"";
    }
}
