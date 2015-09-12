/*
 * Copyright (C) 2014 Tomas Machalek
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

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class DirectoryReaderTest {

	@Test
	public void testDirectoryListing() {
		DirectoryReader d = new DirectoryReader(
				new String[] { "./test-data/dir1" }, 2, null);
		ArrayList<File> chunk1 = new ArrayList<File>();
		ArrayList<File> chunk2 = new ArrayList<File>();

		Iterator<?> i1 = d.getIterator(0);
		while (i1.hasNext()) {
			chunk1.add(new File(String.valueOf(i1.next())));
		}

		Iterator<?> i2 = d.getIterator(1);
		while (i2.hasNext()) {
			chunk2.add(new File(String.valueOf(i2.next())));
		}

		assertEquals(chunk1.get(0).getName(), "file1a-1.txt");
		assertEquals(chunk1.get(0).exists(), true);
		assertEquals(chunk1.get(1).getName(), "file2a-1.txt");
		assertEquals(chunk1.get(1).exists(), true);
		assertEquals(chunk1.size(), 2);

		assertEquals(chunk2.get(0).getName(), "file1-1.txt");
		assertEquals(chunk2.get(0).exists(), true);
		assertEquals(chunk2.get(1).getName(), "file1-2.txt");
		assertEquals(chunk2.get(1).exists(), true);
		assertEquals(chunk2.size(), 2);
	}

	@Test
	public void testDirectoryListingNonExistingRoot() {
		DirectoryReader d = new DirectoryReader(
				new String[] { "./test-data/dir1/noSuchDirectory" }, 2, null);
		Iterator<?> i1 = d.getIterator(0);

		assertEquals(i1.hasNext(), false);
	}

	@Test()
	public void testNonExistingIteratorIdx() {
		DirectoryReader d = new DirectoryReader(
				new String[] { "./test-data/dir1" }, 2, null);

		Iterator<?> itr = d.getIterator(100);
		assertEquals(itr.hasNext(), false);
	}

	@Test
	public void testFilter() {
		String filter = "file[0-9]+-[0-9]+\\.txt";
		DirectoryReader dr = new DirectoryReader(
				new String[] { "./test-data/dir1" }, 1, filter);
		Iterator<String> iter = dr.getIterator(0);
		List<File> ans = new ArrayList<File>();
		while (iter.hasNext()) {
			ans.add(new File(iter.next()));
		}
		assertEquals(ans.size(), 2);
		assertEquals(ans.get(0).getName(), "file1-1.txt");
		assertEquals(ans.get(0).exists(), true);
		assertEquals(ans.get(0).isFile(), true);
		assertEquals(ans.get(1).getName(), "file1-2.txt");
		assertEquals(ans.get(1).exists(), true);
		assertEquals(ans.get(1).isFile(), true);
	}

	/**
	 * Directory reader should be constructed even if the provided filter is
	 * syntactically incorrect.
	 */
	@Test
	public void testIncorrectFilterObjectCreated() {
		String filter = "\\w{4]";
		DirectoryReader dr = new DirectoryReader(
				new String[] { "./test-data/dir1" }, 1, filter);
		assertEquals(dr instanceof DirectoryReader, true);
	}

	/**
	 * Once we try to obtain an iterator from {@link DirectoryReader}
	 * initialized with incorrect regExp, the call should fail.
	 */
	@Test(expectedExceptions = { PatternSyntaxException.class })
	public void testIncorrectFilterFail() {
		String filter = "\\w{4]";
		DirectoryReader dr = new DirectoryReader(
				new String[] { "./test-data/dir1" }, 1, filter);
		dr.getIterator(0);
	}
}
