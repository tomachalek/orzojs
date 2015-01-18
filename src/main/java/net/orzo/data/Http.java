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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http {
	
	private final static Logger LOG = LoggerFactory.getLogger(Http.class);
	
	private final CloseableHttpClient httpClient;

	public Http() {
		this.httpClient = HttpClients.createDefault();
	}
	
	/**
	 * 
	 * @param URL
	 * @return
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
					LOG.warn(String.format("Failed to close response object: %s", e));
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param URL
	 * @param selects
	 * @return
	 * @throws IOException
	 */
	private List<Elements> fetchElementsFromPage(String URL, String...selects) throws IOException {
		final List<Elements> ans = new ArrayList<Elements>();
		final Document doc = Jsoup.connect(URL).get();
		
		for (String select : selects) {
			ans.add(doc.select(select));
		}
		return ans;
	}
	
	/**
	 * 
	 * @param URL
	 * @param selects
	 * @return
	 * @throws IOException
	 */
	public List<List<String>> fetchFromPage(String URL, String...selects) throws IOException {
		final List<List<String>> ans = new ArrayList<List<String>>();
		List<String> selectAns;
		List<Elements> elmsGroups = fetchElementsFromPage(URL, selects);
		for (Elements elements : elmsGroups) {
			selectAns = new ArrayList<>();
			for (Element elm : elements) {
				selectAns.add(elm.text());
			}
			ans.add(selectAns);
		}
		return ans;
	}
	
	/**
	 * 
	 * @param URL
	 * @param selects
	 * @return
	 * @throws IOException
	 */
	public List<List<String>> fetchLinksFromPage(String URL, String...selects) throws IOException {
		List<Elements> elmGroups = fetchElementsFromPage(URL, selects);
		final List<List<String>> ans = new ArrayList<List<String>>();
		Set<String> selectAns;
		List<String> selectAnsList;
		for (Elements elements : elmGroups) {
			selectAns = new HashSet<>();
			for (Element elm : elements) {
				selectAns.add(elm.attr("href"));
			}
			selectAnsList = new ArrayList<String>();
			selectAnsList.addAll(selectAns);
			ans.add(selectAnsList); 
		}
		return ans;
	}

}
