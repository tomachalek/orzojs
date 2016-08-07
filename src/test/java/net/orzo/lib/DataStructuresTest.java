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
package net.orzo.lib;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek<tomas.machalek@gmail.com>
 * 
 */
public class DataStructuresTest {

	@BeforeTest
	public void setUp() {
	}

	@Test
	public void testFlattenMatrix() {
		DataStructures ds = new DataStructures();

		double[][] data = new double[][] { { 1.2, 0.3, 0.4, 7.1 },
				{ 0.1, 0.2, 0.3, 3.1 }, { 10.1, 10.7, 12.8, 10.4 } };

		Object ans = ds.flattenMatrix(data);
		double[] unwrappedAns = (double[])ans; // TODO wrapping???

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				Assert.assertEquals(unwrappedAns[i * data[i].length + j],
						data[i][j], 0.000001);
			}
		}
	}

}
