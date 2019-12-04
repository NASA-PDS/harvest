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

package gov.nasa.pds.harvest.search.security;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Class that provides an interface to the PDS Security Service
 *
 * @author mcayanan
 *
 */
public class SecurityClient {
    private final static String AUTHENTICATE = "authenticate";
    private final static String IS_TOKEN_VALID = "isTokenValid";
    private final static String LOGOUT = "logout";
    private WebResource securityResource;
    private String mediaType;

    /**
     * Constructor
     *
     * @param baseURL The security service url.
     */
    public SecurityClient(String baseURL) {
        ClientConfig clientConfig = new DefaultClientConfig();
        securityResource = Client.create(clientConfig).resource(baseURL);
    }

    /**
     * Determine if the given token is valid.
     *
     * @param token The security token.
     * @return 'true' if the token is valid.
     *
     * @throws SecurityClientException If an error occurred while interacting
     * with the security service.
     */
    public boolean isTokenValid(String token) throws SecurityClientException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tokenid", token);
        ClientResponse response = securityResource.path(IS_TOKEN_VALID)
        .queryParams(params).accept(mediaType).post(ClientResponse.class);

        if(response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            String booleanValue = response.getEntity(String.class);
            booleanValue = booleanValue.split("=")[1].trim();
            return Boolean.parseBoolean(booleanValue);
        } else {
            throw new SecurityClientException(
            "Security service token validation request failed. "
                    + "HTTP Status Code received: "
                    + response.getStatus());
        }
    }

    /**
     * Get a security token.
     *
     * @param username The username.
     * @param password The password.
     *
     * @return A security token.
     *
     * @throws SecurityClientException If an error occurred while interacting
     * with the security service.
     */
    public String authenticate(String username, String password)
    throws SecurityClientException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("username", username);
        params.add("password", password);
        ClientResponse response = securityResource.path(AUTHENTICATE)
        .queryParams(params).accept(mediaType).post(ClientResponse.class);
        if(response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            String token = response.getEntity(String.class);
            token = token.split("=", 2)[1].trim();
            return token;
        } else {
            throw new SecurityClientException(
                    "Security service token ID request failure. "
                    + "HTTP Status Code received: " + response.getStatus());
        }
    }

    /**
     * Logout the authenticated user.
     *
     * @param token The security token.
     *
     * @throws SecurityClientException If an error occurred while interacting
     * with the security service.
     */
    public void logout(String token) throws SecurityClientException {
        ClientResponse response = securityResource.path(LOGOUT)
        .queryParam("subjectid", token).accept(mediaType).post(
                ClientResponse.class);

        if(response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            throw new SecurityClientException(
                    "Security service logout request failure. "
                    + "HTTP Status Code received: " + response.getStatus());
        }
    }
}
