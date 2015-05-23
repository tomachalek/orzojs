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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class SourceCodeTest {

	/**
	 * 
	 */
	@Test
	public void testInit() {
		final String id = "test 1";
		final String code = "var x = 3 + 7;";
		SourceCode src = new SourceCode(id, id, code);
		assertEquals(src.getContents(), code);
		assertEquals(src.getName(), id);
	}

	/**
	 * 
	 */
	@Test
	public void testInitByNullValues() {
		SourceCode src = new SourceCode(null, null, null);
		assertEquals(src.getName(), "unnamed");
		assertEquals(src.getContents(), "");
	}

	/**
	 * @throws IOException
	 * 
	 */
	@Test
	public void testInitByFile() throws IOException {
		SourceCode src = SourceCode.fromFile(new File("test-data/script.js"));
		assertEquals(src.getContents(),
 "function add(x, y) { return x + y; }");
		assertEquals(src.getName(), "script.js");
	}

	@Test
	public void testInitByBOMFile() throws IOException {
		SourceCode src = SourceCode
				.fromFile(new File("test-data/script-bom.js"));
		assertEquals(src.getContents(),
 "function add(x, y) { return x + y; }");
		assertEquals(src.getName(), "script-bom.js");
	}

	/**
	 * @throws IOException
	 * 
	 */
	@Test(expectedExceptions = FileNotFoundException.class)
	public void testInitByNonExistingFile() throws IOException {
		SourceCode.fromFile(new File("thisFileDoesNotExist.js"));
	}

	/**
	 * @throws IOException
	 * 
	 */
	@Test
	public void testInitByResource() throws IOException {
		SourceCode src = SourceCode
				.fromResource("net/orzo/scripting/calculation.js");
		assertEquals(
				src.getContents().trim().replaceAll("\\s+", " "),
				"(function(scope) { 'use strict'; scope.x1 = 100; scope.x2 = 12; scope.multiply = function (a, b) { return a + b; } }(this));");
		assertEquals(src.getName(), "calculation.js");
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = IOException.class)
	public void testInitByNonExistingResource() throws IOException {
		SourceCode.fromResource("net/orzo/scripting/calculation-124135235.js");
	}
}
