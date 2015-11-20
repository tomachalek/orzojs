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
import java.util.Iterator;

/**
 * Generates file pairs where first element comes from one group of directories
 * and second element comes from different group of directories.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TwoGroupFilePairGenerator extends AbstractListGenerator<String[]> {

    private final String[] pathList1;

    private final String[] pathList2;

    private final String filter;

    /**
     *
     * @param pathList1 pair's first item source group
     * @param pathList2 pair's second item source group
     * @param numChunks number of pair chunks to produce
     * @param filter filename filter (regexp pattern)
     */
    public TwoGroupFilePairGenerator(String[] pathList1, String[] pathList2,
            int numChunks, String filter) {
        super(numChunks, new ArrayList<>());
        this.pathList1 = pathList1;
        this.pathList2 = pathList2;
        this.filter = filter;
    }

    /**
     *
     */
    public Iterator<String[]> getIterator(int chunkId) {
        if (isEmpty()) {
            Iterator<String> iter1 = new DirectoryReader(
                    this.pathList1, 1, this.filter).getIterator(0);
            String item1;
            String item2;

            while (iter1.hasNext()) {
                item1 = iter1.next();
                Iterator<String> iter2 = new DirectoryReader(
                        this.pathList2, 1, this.filter).getIterator(0);
                while (iter2.hasNext()) {
                    item2 = iter2.next();
                    addItem(new String[] { item1, item2 });
                }
            }
        }
        return subList(chunkId).iterator();
    }

}
