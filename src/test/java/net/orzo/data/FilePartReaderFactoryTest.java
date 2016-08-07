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
package net.orzo.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class FilePartReaderFactoryTest {

	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInitialOffsetCalc() throws IOException {
		FilePartReaderFactory factory = new FilePartReaderFactory(new File(
				"test-data/text-file.txt"), 5, 4, 0);

		Assert.assertEquals(factory.calcInitOffset(0), 0);
		Assert.assertEquals(factory.calcInitOffset(1), 4);
		Assert.assertEquals(factory.calcInitOffset(2), 8);
		Assert.assertEquals(factory.calcInitOffset(3), 12);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws FilePartReaderFactoryException
	 */
	@Test(expectedExceptions = { FileNotFoundException.class })
	public void testInitialOffsetNonExistingFile() throws IOException,
			FilePartReaderFactoryException {
		FilePartReaderFactory factory = new FilePartReaderFactory(new File(
				"test-data/asdfaswerty24yaqsdfasdf"), 5, 4, 0);
		factory.createInstance(0);
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testTooHighReaderId() throws Exception {
		FilePartReaderFactory factory = new FilePartReaderFactory(new File(
				"test-data/text-file.txt"), 5, 4, 0);
		factory.createInstance(5); // only 0,...,4 can be used
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testNegativeReaderId() throws Exception {
		FilePartReaderFactory factory = new FilePartReaderFactory(new File(
				"test-data/text-file.txt"), 5, 4, 0);
		factory.createInstance(-1); // only 0,...,4 can be used
	}

	@Test
	public void testInitialLineSkip() throws Exception {
		FilePartReaderFactory factory = new FilePartReaderFactory(new File(
				"test-data/text-file.txt"), 3, 7, 3);
		FilePartReader reader = factory.createInstance(0);
		ArrayList<String> ans = new ArrayList<String>();

		while (reader.hasNext()) {
			ans.add(reader.next());
		}
		Assert.assertEquals(ans.get(0), "this is line 3");
		Assert.assertEquals(ans.get(ans.size() - 1), "this is line 9");
		Assert.assertEquals(ans.size(), 7);
	}
}
