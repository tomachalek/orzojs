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
package net.orzo;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class IntermediateResultsTest {

	@Test
	public void testAddRetrieveValues() {
		IntermediateResults ir = new IntermediateResults();
		Object value1 = new Object();
		Object value2 = new Object();
		Object value3 = new Object();

		ir.add("foo", value1);
		ir.add("bar", value2);
		ir.add("bar", value3);

		Assert.assertTrue(ir.keys().contains("foo"));
		Assert.assertTrue(ir.keys().contains("bar"));
		Assert.assertEquals(ir.keys().size(), 2);

		Assert.assertEquals(ir.values("foo").get(0), value1);
		Assert.assertEquals(ir.values("foo").size(), 1);

		Assert.assertEquals(ir.values("bar").get(0), value2);
		Assert.assertEquals(ir.values("bar").get(1), value3);
		Assert.assertEquals(ir.values("bar").size(), 2);
	}

	@Test(expectedExceptions = { NullPointerException.class })
	public void testAddNullKey() {
		IntermediateResults ir = new IntermediateResults();
		ir.add(null, new Object());
	}

	@Test
	public void testRetrieveNonExisting() {
		IntermediateResults ir = new IntermediateResults();
		Object value1 = new Object();
		ir.add("foo", value1);

		Assert.assertEquals(ir.values("bar").size(), 0);
	}

	@Test
	public void testAddAll() {
		IntermediateResults ir = new IntermediateResults();
		IntermediateResults ir2 = new IntermediateResults();
		Object value1 = new Object();
		Object value2 = new Object();
		Object value3 = new Object();
		ir.add("foo", value1);
		ir2.add("foo2", value2);
		ir2.add("bar2", value3);

		ir.addAll(ir2);

		Assert.assertTrue(ir.keys().contains("foo"));
		Assert.assertTrue(ir.keys().contains("foo2"));
		Assert.assertTrue(ir.keys().contains("bar2"));
		Assert.assertEquals(ir.keys().size(), 3);

		Assert.assertEquals(ir.values("foo").get(0), value1);
		Assert.assertEquals(ir.values("foo").size(), 1);
		Assert.assertEquals(ir.values("foo2").get(0), value2);
		Assert.assertEquals(ir.values("foo2").size(), 1);
		Assert.assertEquals(ir.values("bar2").get(0), value3);
		Assert.assertEquals(ir.values("bar2").size(), 1);
	}

	@Test
	public void testAddAllAddEmpty() {
		IntermediateResults ir = new IntermediateResults();
		IntermediateResults ir2 = new IntermediateResults();
		Object value1 = new Object();
		ir.add("foo", value1);

		ir.addAll(ir2);

		Assert.assertTrue(ir.keys().contains("foo"));
		Assert.assertEquals(ir.keys().size(), 1);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testAddAllAddNull() {
		IntermediateResults ir = new IntermediateResults();
		IntermediateResults ir2 = null;
		Object value1 = new Object();
		ir.add("foo", value1);

		ir.addAll(ir2);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testAddAllAddSelf() {
		IntermediateResults ir = new IntermediateResults();
		Object value1 = new Object();
		Object value2 = new Object();
		ir.add("foo", value1);
		ir.add("foo2", value2);
		ir.addAll(ir);
	}

}
