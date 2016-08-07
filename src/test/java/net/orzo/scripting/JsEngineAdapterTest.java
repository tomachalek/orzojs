/*
 * Copyright (C) 2013 Tomas Machalek
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
package net.orzo.scripting;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import javax.script.ScriptException;

import net.orzo.SharedServices;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class JsEngineAdapterTest {

	/**
	 * 
	 */
	private JsEngineAdapter js;

	/**
	 * 
	 */
	@BeforeMethod
	public void setUp() {
		EnvParams envParams = new EnvParams();
		this.js = new JsEngineAdapter(envParams, new SharedServices(null));
	}

	/**
	 * @throws IOException
	 * @throws ScriptException
	 */
	@Test
	public void testNormalFunction() throws IOException, ScriptException {
		js.beginWork();
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
		assertEquals(js.get("pi").toString(), "3.1416"); // just a simple test
															// that main context
															// values are
															// available
		js.endWork();
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ScriptException
	 */
	@Test(expectedExceptions = ScriptConfigurationException.class)
	public void testInvalidInitialization() throws IOException, ScriptException {
		// normally, beginWork(...) would be here
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
	}

}
