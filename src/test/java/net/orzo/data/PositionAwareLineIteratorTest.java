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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class PositionAwareLineIteratorTest {

	@Test
	public void testZeroSkip() throws Exception {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");

		lai.skipTo(0);
		Assert.assertEquals(lai.next(), "this is line 0");
		Assert.assertEquals(lai.getCurrLine(), 0);
	}

	@Test
	public void testFirstLineSkip() throws Exception {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");

		lai.skipTo(1);
		Assert.assertEquals(lai.next(), "this is line 1");
		Assert.assertEquals(lai.getCurrLine(), 1);
	}

	@Test
	public void testInMiddleSkipping() throws Exception {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");

		lai.skipTo(12);
		Assert.assertEquals(lai.next(), "this is line 12");
		Assert.assertEquals(lai.getCurrLine(), 12);
	}

	@Test
	public void testOverSkip() throws Exception {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");

		lai.skipTo(1200);
		Assert.assertFalse(lai.hasNext());
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testSkipToNegativeLine() throws Exception {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");

		lai.skipTo(-5);
	}

	@Test(expectedExceptions = { FileNotFoundException.class })
	public void testNonExistingFile() throws Exception {
		PositionAwareLineIterator.create("test-data/24tqwsdgasdfwsdfa.txt");

	}

	@Test
	public void testSkipBy() throws IOException {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");
		for (int i = 0; i < 5; i++) {
			lai.next();
		}
		lai.skipBy(10);
		Assert.assertEquals(lai.next(), "this is line 14");
		Assert.assertEquals(lai.getCurrLine(), 14);
	}

	@Test
	public void testSkipByZero() throws IOException {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");
		for (int i = 0; i < 5; i++) {
			lai.next();
		}
		lai.skipBy(0);
		Assert.assertEquals(lai.next(), "this is line 5");
		Assert.assertEquals(lai.getCurrLine(), 5);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testSkipByNegative() throws IOException {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt");
		for (int i = 0; i < 5; i++) {
			lai.next();
		}
		lai.skipBy(-1);
	}

	@Test
	public void testSkipByTooMuch() throws IOException {
		PositionAwareLineIterator lai = PositionAwareLineIterator
				.create("test-data/text-file.txt"); // there are 24 lines there
		lai.skipBy(25);
		Assert.assertFalse(lai.hasNext());
		Assert.assertEquals(lai.getCurrLine(), 23); // i.e. we have moved to the
													// last line and there is
													// nothing left
	}
}
