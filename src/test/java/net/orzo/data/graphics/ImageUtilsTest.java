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
package net.orzo.data.graphics;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.testng.annotations.Test;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class ImageUtilsTest {

	/**
	 * @throws IOException
	 * 
	 */
	@Test
	public void testImageToArray() throws IOException {
		GreyscalePicture p = GreyscalePicture.load("test-data/image-1.png");
		int[][] data = p.toArray();
		assertEquals(data[0][0], 0);
		assertEquals(data[0][4], 0);

		assertEquals(data[1][0], 193);
		assertEquals(data[1][4], 193);

		assertEquals(data[2][0], 239);
		assertEquals(data[2][4], 239);

		assertEquals(data[3][0], 255);
		assertEquals(data[3][4], 255);
	}

}
