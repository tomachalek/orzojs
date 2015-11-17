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
public class FilePairGenerator extends AbstractListGenerator<String[]> {


    private final DirectoryReader directoryReader;

    /**
     *
     * @param pathList
     * @param numChunks
     * @param filter
     */
    public FilePairGenerator(String[] pathList, int numChunks, String filter) {
        super(numChunks, new ArrayList<>());
        this.directoryReader = new DirectoryReader(pathList, 1, filter);
    }

    /**
     *
     * @param chunkId
     * @return
     */
    public Iterator<String[]> getIterator(int chunkId) {
        if (isEmpty()) {
            Iterator<String> iter = this.directoryReader.getIterator(0);
            final List<String> visitedItems = new ArrayList<>();
            String currItem;

            while (iter.hasNext()) {
                currItem = iter.next();
                for (String visitedItem : visitedItems) {
                    addItem(new String[] { currItem, visitedItem });
                }
                visitedItems.add(currItem);
            }
        }
        return subList(chunkId).iterator();
    }

}
