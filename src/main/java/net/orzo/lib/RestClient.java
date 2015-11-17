/*
 * Copyright (c) 2015 Tomas Machalek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.orzo.lib;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import javax.ws.rs.core.MediaType;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class RestClient {

    private Client client;


    RestClient() {
        this.client = Client.create();
        this.client.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
    }

    WebResource.Builder createWebResourceBuilder(String url) {
        WebResource resource = this.client.resource(url);
        return resource.accept(
                MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_XML_TYPE,
                MediaType.TEXT_PLAIN_TYPE,
                MediaType.APPLICATION_OCTET_STREAM_TYPE,
                MediaType.TEXT_HTML_TYPE
        );
    }

    public String get(String url) {
        return createWebResourceBuilder(url).get(String.class);
    }

    public String put(String url, String body) {
        return createWebResourceBuilder(url).put(String.class, body);
    }

    public String post(String url, String body) {
        return createWebResourceBuilder(url).post(String.class, body);
    }

    public String delete(String url) {
        return createWebResourceBuilder(url).delete(String.class);
    }

    public String head(String url) {
        return createWebResourceBuilder(url).head().toString();
    }
}
