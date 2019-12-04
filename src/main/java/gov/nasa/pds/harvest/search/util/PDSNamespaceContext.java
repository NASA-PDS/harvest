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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import gov.nasa.pds.harvest.search.policy.Namespace;

/**
 * Class that provides support for handling namespaces in PDS4
 * data products.
 *
 * @author mcayanan
 *
 */
public class PDSNamespaceContext implements NamespaceContext {
    private Map<String, String> namespaces;

    /**
     * Constructor.
     *
     */
    public PDSNamespaceContext() {
        this.namespaces = new HashMap<String, String>();
        this.namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");
    }

    /**
     * Constructor.
     *
     * @param namespaces A list of namespaces to support.
     */
    public PDSNamespaceContext(List<Namespace> namespaces) {
        this();
        for (Namespace ns : namespaces) {
            this.namespaces.put(ns.getPrefix(), ns.getUri());
        }
    }

    /**
     * Adds a namespace.
     *
     * @param namespace A namespace to support.
     */
    public void addNamespace(Namespace namespace) {
        this.namespaces.put(namespace.getPrefix(), namespace.getUri());
    }

    /**
     * Gets the namespace URI.
     *
     * @param prefix The prefix
     *
     * @return The URI to the given prefix. Returns the PDS namespace URI
     * if the given prefix is empty or null.
     */
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null || "".equals(prefix)) {
            return "";
        } else {
            return namespaces.get(prefix);
        }
    }

    /**
     * Method not needed
     *
     */
    @Override
    public String getPrefix(String arg0) {
        // Method not necessary
        return null;
    }

    /**
     * Method not needed
     *
     */
    @Override
    public Iterator getPrefixes(String arg0) {
        // Method not necessary
        return null;
    }

}
