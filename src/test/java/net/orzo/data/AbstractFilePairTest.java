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
import java.util.Set;

public abstract class AbstractFilePairTest {


	private final Set<File[]> expected;
	
	public AbstractFilePairTest() {
		this.expected = new HashSet<File[]>();
	}
	
	public void addExpected(String[] pair) {
		this.expected.add(new File[]{ new File(pair[0]), new File(pair[1]) });
	}
		
	public boolean contains(String[] pair) {
		File[]  filePair = new File[]{new File(pair[0]), new File(pair[1])};
		for (File[] item : this.expected) {
			if (item[0].getAbsolutePath().equals(filePair[0].getAbsolutePath()) 
					&& item[1].getAbsolutePath().equals(filePair[1].getAbsolutePath())) {
				return true;
			}
		}
		return false;
	}
	
	public int expectedSize() {
		return this.expected.size();
	}
}
