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

import net.orzo.CalculationPhase;

import org.mozilla.javascript.EcmaError;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class JsEngineFacadeTest {

	/**
	 * 
	 */
	private JsEngineFacade js;

	/**
	 * 
	 */
	@BeforeMethod
	public void setUp() {
		EnvParams envParams = new EnvParams();
		envParams.calculationPhase = CalculationPhase.OTHER;
		this.js = new JsEngineFacade(envParams,
				new DummyScriptGlobalFuncHandler());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ScriptProcessingException
	 */
	@Test
	public void testNormalFunction() throws IOException,
			ScriptProcessingException {
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
	 * @throws ScriptProcessingException
	 */
	@Test(expectedExceptions = ScriptConfigurationException.class)
	public void testInvalidInitialization() throws IOException,
			ScriptProcessingException {
		// normally, beginWork(...) would be here
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
	}

	/**
	 * 
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws ScriptProcessingException
	 */
	@Test
	public void testCallFunction() throws IOException, NoSuchMethodException,
			ScriptProcessingException {
		js.beginWork();
		js.runCode(SourceCode.fromResource("net/orzo/scripting/calculation.js"));
		Object ans = js.invokeFunction("multiply", 4.3, 2);
		assertEquals(Double.valueOf(ans.toString()), 8.6, 0.0001);
		js.endWork();
	}

	/**
	 * 
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws ScriptProcessingException
	 */
	@Test(expectedExceptions = TypeHandlingException.class)
	public void testCallNonExistingFunction() throws IOException,
			NoSuchMethodException, ScriptProcessingException {
		js.beginWork();
		js.runCode(SourceCode.fromResource("net/orzo/scripting/calculation.js"));
		js.invokeFunction("multiply_foobar", 4.3, 2);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws ScriptProcessingException
	 */
	@Test
	public void testCallMethod() throws IOException, NoSuchMethodException,
			ScriptProcessingException {
		js.beginWork();
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
		Object ans = js.invokeMethod("person", "sayHello", new Object[] {});
		assertEquals(ans.toString(), "John says hello!");
		js.endWork();
	}

	/**
	 * 
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws ScriptProcessingException
	 * @todo this is not very consistent with other errors throwing
	 *       TypeHandlingException
	 */
	@Test(expectedExceptions = EcmaError.class)
	public void testCallNonExistingMethod() throws IOException,
			NoSuchMethodException, ScriptProcessingException {
		js.beginWork();
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
		js.invokeMethod("person", "sayHello13039502135", new Object[] {});
	}

	/**
	 * 
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws ScriptProcessingException
	 */
	@Test(expectedExceptions = TypeHandlingException.class)
	public void testCallNonExistingObjectMethod() throws IOException,
			NoSuchMethodException, ScriptProcessingException {
		js.beginWork();
		js.runCode(SourceCode
				.fromResource("net/orzo/scripting/simple-script.js"));
		js.invokeMethod("person4623456724365", "sayHello", new Object[] {});
	}

}
