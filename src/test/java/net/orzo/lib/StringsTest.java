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
	
	@Test
	public void testHash() {
		String inValue = "lorem ipsum dolor sit amet";
		Strings lib = new Strings();
		
		Assert.assertEquals(lib.hash(inValue, "md5"), "201730d4278e576b25515bd90c6072d3");
		Assert.assertEquals(lib.hash(inValue, "sha1"), "530ec607b1e73e922f3767aa2ca64f05b5a1dc31");
		Assert.assertEquals(lib.hash(inValue, "sha256"), "2f8586076db2559d3e72a43c4ae8a1f5957abb23ca4a1f46e380dd640536eedb");
		Assert.assertEquals(lib.hash(inValue, "sha384"), "00548f2253566369e807f0f4f7bf02dac13bde6fb603ec064bdb9a6075df18efd4169e76f18dccffaa3646e5fca8c05e");
		Assert.assertEquals(lib.hash(inValue, "sha512"), "bafa0732d3b1a1d95431bd6fff46b35ac6b60c64ac8ea8b11cb05f7c1a706469aa04c181172bd5e303c3a1f19eef35469500fe9866e6b4c7bbc12759fee8e735");
	}
	
	@Test(expectedExceptions={RuntimeException.class})
	public void testHashUnknownAlgorithm() {
		Strings lib = new Strings();
		lib.hash("foo", "crc32");
	}
	
	@Test
	public void testHashAcceptUpperAndLowerAlg() {
		String inVal = "foo";
		Strings lib = new Strings();
		Assert.assertTrue(lib.hash(inVal, "md5").equals(lib.hash(inVal, "MD5")));
	}
	
	@Test(expectedExceptions={RuntimeException.class})
	public void testHashNullNotAccepted() {
		Strings lib = new Strings();
		lib.hash(null, "sha1");
	}

}
