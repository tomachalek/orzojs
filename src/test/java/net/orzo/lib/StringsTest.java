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
package net.orzo.lib;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class StringsTest {
	
	private PrintStream originalOut;
	
	private PrintStream mockOut;
	
	private ByteArrayOutputStream out;
	
	
	@BeforeSuite
	public void beforeSuite() {
		System.setProperty("logback.configurationFile", "./logback-tests.xml");
		Locale.setDefault(Locale.US);
		this.originalOut = System.out;
	}
	
	@AfterMethod
	public void tearDown() {
		System.out.flush();
		System.setOut(this.originalOut);
	}
	
	@BeforeMethod
	public void setUp() {
		this.out = new ByteArrayOutputStream(); 
		this.mockOut = new PrintStream(new BufferedOutputStream(this.out));
		System.setOut(this.mockOut);
	}
	
	
	@Test
	public void testPrint() {
		final String id = "<Hi, this is me>";
		Object foo = new Object() {
			@Override
			public String toString() {
				return id;
			}
		};
		Strings lib = new Strings();
		lib.print(foo);
		this.mockOut.flush();
		Assert.assertEquals(this.out.toString(), id + String.format("%n"));
	}

	
	@Test
	public void testPrintf() {				
		Strings lib = new Strings();
		lib.printf("foo %s, bar %1.3f, baz %04d", "?", 3.1416, 3.7);
		this.mockOut.flush();
		Assert.assertEquals(this.out.toString(), "foo ?, bar 3.142, baz 0004");
		
	}
	

	@Test
	public void testSprintf() {
		Strings lib = new Strings();
		String ans = (String)lib.sprintf("foo %s, bar %1.3f, baz %04d", "?", 3.1416, 3.7);
		Assert.assertEquals(ans, "foo ?, bar 3.142, baz 0004");
	}

}
