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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.script.ScriptException;

import net.orzo.SharedServices;
import net.orzo.scripting.EnvParams;
import net.orzo.scripting.JsEngineAdapter;
import net.orzo.scripting.SourceCode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class WebTest {

	private JsEngineAdapter engine;

	@BeforeTest
	public void setUpTest() {
		EnvParams envParams = new EnvParams();
		this.engine = new JsEngineAdapter(envParams, new SharedServices(null));
		this.engine.beginWork();
	}

	private Document loadPage() throws IOException {
		return Jsoup.parse(new File("test-data/page1.html"), "UTF-8");
	}

	@Test
	public void testFindOnPagePassRootElement() throws ScriptException,
			IOException, NoSuchMethodException {
		Document root = loadPage();
		this.engine.put("startFrom", root);
		this.engine
				.runCode(SourceCode.fromResource("net/orzo/data/webtest.js"));
		this.engine.runFunction("modifyDivOldElements");

		for (Iterator<Element> itr = root.select("div.old").iterator(); itr
				.hasNext();) {
			Element elm = itr.next();
			Assert.assertTrue(elm.hasClass("foo"));
		}
	}

	@Test
	public void testFindOnPagePassDocument() throws ScriptException,
			IOException, NoSuchMethodException {
		Element root = loadPage().body();
		this.engine.put("startFrom", root);
		this.engine
				.runCode(SourceCode.fromResource("net/orzo/data/webtest.js"));
		this.engine.runFunction("modifyDivOldElements");

		for (Iterator<Element> itr = root.select("div.old").iterator(); itr
				.hasNext();) {
			Element elm = itr.next();
			Assert.assertTrue(elm.hasClass("foo"));
		}
	}

	@Test
	public void testFindOnPageEmptyFunction() throws ScriptException,
			IOException, NoSuchMethodException {
		Document root = loadPage();
		this.engine.put("startFrom", root);
		this.engine
				.runCode(SourceCode.fromResource("net/orzo/data/webtest.js"));
		this.engine.runFunction("modifyDivOldElementsWithoutCallback");

		for (Iterator<Element> itr = root.select("div.old").iterator(); itr
				.hasNext();) {
			Element elm = itr.next();
			Assert.assertTrue(elm.hasClass("foo"));
		}
	}
}
