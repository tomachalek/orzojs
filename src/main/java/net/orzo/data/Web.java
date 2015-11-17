/*
 * Copyright (C) 2015 Tomas Machalek
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

package net.orzo.data;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jdk.nashorn.internal.runtime.ScriptFunction;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

@SuppressWarnings("restriction")
public class Web {

    private final static Logger LOG = LoggerFactory.getLogger(Web.class);

    private final CloseableHttpClient httpClient;

    public Web() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     *
     */
    public String get(String URL) {
        HttpGet httpget = new HttpGet(URL);
        CloseableHttpResponse response = null;
        HttpEntity httpEntity;
        String result = null;

        try {
            response = this.httpClient.execute(httpget);
            httpEntity = response.getEntity();
            if (httpEntity != null) {
                httpEntity = new BufferedHttpEntity(httpEntity);
                result = EntityUtils.toString(httpEntity);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.warn(String.format(
                            "Failed to close response object: %s", e));
                }
            }
        }
        return result;
    }

    /**
     *
     */
    public Document loadWebsite(String URL) {
        try {
            return Jsoup.connect(URL).get();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public Document parseHTML(String html) {
        return Jsoup.parse(html);
    }

    /**
     *
     */
    public List<Element> queryPage(Element root, String select,
            ScriptFunction fn) {
        MethodHandle mh;
        Element curr;
        List<Element> ans = null; // returns null in case fn is null

        try {
            if (fn != null) {
                for (Iterator<Element> iter = root.select(select).iterator(); iter
                        .hasNext();) {
                    curr = iter.next();
                    mh = fn.getBoundInvokeHandle(curr);
                    mh.invoke(curr);
                }

            } else {
                ans = new ArrayList<>();
                Iterators.addAll(ans, root.select(select).iterator());
            }
            return ans;

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Element> queryPage(Document document, String select,
            ScriptFunction fn) {
        return queryPage(document.body(), select, fn);
    }
}
