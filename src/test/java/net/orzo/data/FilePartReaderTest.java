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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class FilePartReaderTest {

	private PositionAwareLineIterator createFilePartReader(String path)
			throws IllegalArgumentException, IOException {
		return PositionAwareLineIterator.create(path);
	}

	private PositionAwareLineIterator createFilePartReader()
			throws IllegalArgumentException, IOException {
		return PositionAwareLineIterator.create("test-data/text-file.txt");
	}

	/**
	 * 
	 */
	@Test
	public void testProperChunkDistributionAndSize()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader rf = new FilePartReader(lit, 4, 2); // text-file.txt has
															// 24 lines
		List<String> data = new ArrayList<String>();

		while (rf.hasNext()) {
			data.add(rf.next());
		}
		Assert.assertEquals(data.size(), 6);
		Assert.assertEquals(data.get(0), "this is line 0");
		Assert.assertEquals(data.get(1), "this is line 1");
		Assert.assertEquals(data.get(2), "this is line 8");
		Assert.assertEquals(data.get(3), "this is line 9");
		Assert.assertEquals(data.get(4), "this is line 16");
		Assert.assertEquals(data.get(5), "this is line 17");
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = { FilePartReaderMisconfiguration.class })
	public void testNegativeNumOfReadersHasNextCall()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, -1, 2);
		fpr.hasNext();
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 */
	@Test(expectedExceptions = { FilePartReaderMisconfiguration.class })
	public void testNegativeNumOfReadersNextCall()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, -1, 2);
		fpr.next();
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 */
	@Test(expectedExceptions = { FilePartReaderMisconfiguration.class })
	public void testNegativeChunkSizeHasNextCall()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, 1, -1);
		fpr.hasNext();
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 */
	@Test(expectedExceptions = { FilePartReaderMisconfiguration.class })
	public void testNegativeChunkSizeNextCall()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, 1, -1);
		fpr.next();
	}

	/**
	 * 
	 */
	@Test
	public void testNonEmptyFileIterationWithOverChunkSize()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, 1, 10000);
		List<String> data = new ArrayList<String>();

		while (fpr.hasNext()) {
			data.add(fpr.next());
		}

		Assert.assertEquals(data.size(), 24);
	}

	/**
	 * 
	 */
	@Test
	public void testNonEmptyFileIterationWithUnderChunkSize()
			throws IllegalArgumentException, IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, 1, 10);
		List<String> data = new ArrayList<String>();

		while (fpr.hasNext()) {
			data.add(fpr.next());
		}

		Assert.assertEquals(data.size(), 24);
	}

	/**
	 * 
	 */
	@Test(expectedExceptions = { NoSuchElementException.class })
	public void testAccessNonExistingItem() throws IllegalArgumentException,
			IOException {
		PositionAwareLineIterator lit = createFilePartReader();
		FilePartReader fpr = new FilePartReader(lit, 1, 1);
		for (int i = 0; i < 24; i++) {
			fpr.next();
		}
		fpr.next(); // this throws NoSuchElementException
	}

	/**
	 * 
	 */
	@Test
	public void testEmptyFileIteration() throws IllegalArgumentException,
			IOException {
		PositionAwareLineIterator lit = createFilePartReader("test-data/empty.txt");
		FilePartReader fpr = new FilePartReader(lit, 1, 10000);
		Assert.assertFalse(fpr.hasNext());
	}

}
