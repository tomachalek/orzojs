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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class to generate all pairs of different files (N * (N-1) items) found in a
 * specified directory.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class FilePairGenerator {

	private final DirectoryReader directoryReader;

	private final int numChunks;

	private List<String[]> pairList;

	private int itemsPerChunk;

	public FilePairGenerator(String[] pathList, int numChunks, String filter) {
		this.numChunks = numChunks;
		this.directoryReader = new DirectoryReader(pathList, 1, filter);
		this.pairList = null;
		this.itemsPerChunk = 0;
	}

	public Iterator<String[]> getIterator(int chunkId) {
		int rightIdx;

		if (this.pairList == null) {
			this.pairList = new ArrayList<String[]>();
			Iterator<String> iter = this.directoryReader.getIterator(0);
			ArrayList<String> items = new ArrayList<String>();

			while (iter.hasNext()) {
				items.add(iter.next());
			}
			for (int i = 0; i < items.size(); i++) {
				for (int j = 0; j < i; j++) {
					this.pairList
							.add(new String[] { items.get(i), items.get(j) });
				}
			}

			this.itemsPerChunk = (int) Math.ceil(this.pairList.size()
					/ this.numChunks);
		}

		rightIdx = Math.min(this.itemsPerChunk * (chunkId + 1),
				this.pairList.size());

		return this.pairList.subList(chunkId * this.itemsPerChunk, rightIdx)
				.iterator();
	}

}
