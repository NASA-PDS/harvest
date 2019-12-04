// Copyright 2019, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other
// materials provided with the distribution.
// * Neither the name of Caltech nor its operating division, the Jet Propulsion
// Laboratory, nor the names of its contributors may be used to endorse or
// promote products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

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
