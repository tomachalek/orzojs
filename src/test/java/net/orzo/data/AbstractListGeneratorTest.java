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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class AbstractListGeneratorTest {
	
	private FooListGenerator createFooListGenerator(int listSize, int numChunks) {
		final FooListGenerator listGen = new FooListGenerator(numChunks, new ArrayList<Integer>());		
		for (int i = 0; i < listSize; i++) {
			listGen.addItem(i);
		}
		return listGen;
	}	

	@Test
	public void testInitialization() {
		final int listSize = 0;
		final int numChunks = 4;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		
		Assert.assertEquals(listGen.size(), listSize);
		Assert.assertTrue(listGen.isEmpty());
		Assert.assertEquals(listGen.getNumChunks(), numChunks); 
	}	
	
	@Test
	public void testInitializationWithData() {
		final int listSize = 12;
		final int numChunks = 4;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		
		Assert.assertEquals(listGen.size(), listSize);
		Assert.assertFalse(listGen.isEmpty());
		Assert.assertEquals(listGen.getNumChunks(), numChunks); 
	}
	
	@Test
	public void testChunksCalculation() {
		final int listSize = 12;
		final int numChunks = 4;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		List<Integer> expected;
		
		expected = Arrays.asList(new Integer[]{0, 1, 2});
		Assert.assertEquals(listGen.subList(0), expected); 
		expected = Arrays.asList(new Integer[]{3, 4, 5});
		Assert.assertEquals(listGen.subList(1), expected);
		expected = Arrays.asList(new Integer[]{6, 7, 8});
		Assert.assertEquals(listGen.subList(2), expected);
		expected = Arrays.asList(new Integer[]{9, 10, 11});
		Assert.assertEquals(listGen.subList(3), expected);
	}
	
	@Test
	public void testChunksCalculationNonDivisibleSize() {
		final int listSize = 11;
		final int numChunks = 2;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		List<Integer> expected;
		
		expected = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5});
		Assert.assertEquals(listGen.subList(0), expected); 
		expected = Arrays.asList(new Integer[]{6, 7, 8, 9, 10});
		Assert.assertEquals(listGen.subList(1), expected);	
	}
		
	@Test(expectedExceptions={RuntimeException.class})
	public void testFetchNonExistingChunk() {
		final int listSize = 11;
		final int numChunks = 2;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		List<Integer> expected;
		expected = Arrays.asList(new Integer[]{10});
		Assert.assertEquals(listGen.subList(3), expected);		
	}
	
	@Test(expectedExceptions={RuntimeException.class})
	public void testFetchFirstChunkFromEmpty() {
		final int listSize = 0;
		final int numChunks = 0;
		final FooListGenerator listGen = createFooListGenerator(listSize, numChunks);
		List<Integer> expected;
		expected = Arrays.asList(new Integer[]{});
		Assert.assertEquals(listGen.subList(0), expected);		
	}

}
