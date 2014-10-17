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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class JsToolsTest extends GeneralScriptingTest {

	/**
	 * 
	 * @throws IOException
	 */
	@BeforeMethod
	public void setUp() throws IOException {
		runScriptResource("net/orzo/scripting/simple-script.js");
	}

	/**
	 * 
	 */
	@Test
	public void testImportExistingObject() {
		Object value = getValue("person");
		Map<String, Object> map = JsTools.importObject(value);
		assertEquals(map.size(), 5);
		assertEquals(map.get("firstName"), "John");
		assertEquals(map.get("lastName"), "Doe");
		assertEquals(map.get("age"), 37);
		assertEquals(map.get("weight"), 83.9);
	}

	/**
	 * 
	 */
	@Test
	public void testImportNonExistingObject() {
		Object value = getValue("asdf"); // = non existing entry
		Map<String, Object> map = JsTools.importObject(value);
		assertNull(map);
	}

	/**
	 * Tests proper import of a JS array, including recursive structures
	 */
	@Test
	public void testImportExistingArray() {
		Object value = getValue("itemList");
		Object[] items = JsTools.importArray(value);
		assertEquals(items.length, 4); // see simple-script.js
		assertTrue(items[0] instanceof Map); // enough for this item, its
												// details are tested elsewhere
		assertEquals(items[1], 2);
		assertEquals(items[2], "second");
		assertTrue(items[3] instanceof Object[]);

		assertEquals(((Object[]) items[3])[0], 3);
		assertEquals(((Object[]) items[3])[1], "item four");
	}

	/**
	 * 
	 */
	@Test
	public void testImportNonExistingArray() {
		Object value = getValue("itemListInNoWay");
		Object[] items = JsTools.importArray(value);
		assertNull(items);
	}

	/**
	 * 
	 */
	@Test
	public void testImportNumber() {
		Object n1 = 3.1416;
		Object n2 = 3000;
		Object n3 = 2.7182f;
		Object n4 = "4702345";
		assertEquals(JsTools.importNumber(n1), 3.1416, 4);
		assertEquals(JsTools.importNumber(n2), 3000, 4);
		assertEquals(JsTools.importNumber(n3), 2.7182, 4);
		assertEquals(JsTools.importNumber(n4), Double.NaN);
		assertEquals(JsTools.importNumber(null), Double.NaN);
	}

	/**
	 * 
	 */
	@Test
	public void testImportNativeObjectArray() {
		Object value = getValue("objectList");
		List<NativeObject> objectList = JsTools.importNativeObjectArray(value);
		assertEquals(objectList.size(), 3);
		assertEquals(objectList.get(0).get("type", getScope()), "dog");
		assertEquals(objectList.get(1).get("type", getScope()), "cat");
		assertEquals(objectList.get(2).get("type", getScope()), "squirrel");
	}

	/**
	 * 
	 */
	@Test
	public void testImportNonExistingNativeObjectArray() {
		Object value = getValue("objectListInNoWay");
		List<NativeObject> objectList = JsTools.importNativeObjectArray(value);
		assertNull(objectList);

	}

	/**
	 * 
	 */
	@Test
	public void testImportJavaObjectArray() {
		Object value = getValue("javaObjectList");
		List<Vehicle> items = JsTools.importJavaObjectArray(value,
				Vehicle.class);

		assertEquals(items.size(), 2);
		assertTrue(items.get(0) instanceof Vehicle);
		assertTrue(items.get(1) instanceof Bus);
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = TypeHandlingException.class)
	public void testImportJavaObjectArrayError() {
		Object value = getValue("javaObjectList");
		JsTools.importJavaObjectArray(value, Bus.class);
	}
}
