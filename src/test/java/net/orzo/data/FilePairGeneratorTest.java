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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class FilePairGeneratorTest {
	
	private Set<File[]> expected;
	
	
	@BeforeClass
	public void setupExpected() {
		expected = new HashSet<>();
		expected.add(new File[]{ new File("./test-data/dir1/dir2a/file2a-1.txt"), 
				new File("./test-data/dir1/dir1a/file1a-1.txt") });
		expected.add(new File[]{ new File("./test-data/dir1/file1-1.txt"), 
				new File("./test-data/dir1/dir1a/file1a-1.txt") });
		expected.add(new File[]{ new File("./test-data/dir1/file1-1.txt"), 
				new File("./test-data/dir1/dir2a/file2a-1.txt") });
		expected.add(new File[]{ new File("./test-data/dir1/file1-2.txt"), 
				new File("./test-data/dir1/dir1a/file1a-1.txt") });
		expected.add(new File[]{ new File("./test-data/dir1/file1-2.txt"), 
				new File("./test-data/dir1/dir2a/file2a-1.txt") });
		expected.add(new File[]{ new File("./test-data/dir1/file1-2.txt"), 
				new File("./test-data/dir1/file1-1.txt") });
	}
	
	private boolean contains(String[] pair) {
		File[]  filePair = new File[]{new File(pair[0]), new File(pair[1])};
		for (File[] item : this.expected) {
			if (item[0].getAbsolutePath().equals(filePair[0].getAbsolutePath()) 
					&& item[1].getAbsolutePath().equals(filePair[1].getAbsolutePath())) {
				return true;
			}
		}
		return false;
	}	
	

	@Test
	public void testNonEmptyDirectory() {
		FilePairGenerator fpg = new FilePairGenerator(
				new String[]{"./test-data/dir1"}, 1, null);
		Iterator<String[]> itr = fpg.getIterator(0);		
		int total = 0;		
		while (itr.hasNext()) {
			Assert.assertTrue(contains(itr.next()));
			total++;
		}
		Assert.assertEquals(total, this.expected.size());
	}

}
