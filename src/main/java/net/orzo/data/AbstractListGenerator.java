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

import java.util.List;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 * @param <T>
 */
public abstract class AbstractListGenerator<T> {


    private List<T> itemList;

    private final int numChunks;

    private int itemsPerChunk = -1;

    /**
     *
     * @param numChunks
     * @param initialList
     */
    public AbstractListGenerator(int numChunks, List<T> initialList) {
        this.numChunks = numChunks;
        this.itemList = initialList;
    }

    /**
     *
     * @return
     */
    public int getNumChunks() {
        return this.numChunks;
    }

    /**
     *
     * @return
     */
    private void autoSetItemsPerChunk() {
        if (this.numChunks < 1) { // we must check numChunks first as (int)NaN == 0 (see calc. below)
            throw new RuntimeException(String.format("Value numChunks = %d incorrect (must be >= 1)", this.numChunks));
        }
        this.itemsPerChunk = (int) Math.ceil((double)this.itemList.size() / this.numChunks);
    }

    /**
     *
     * @return
     */
    private int getItemsPerChunk() {
        if (this.itemsPerChunk == -1) {
            autoSetItemsPerChunk();
        }
        return this.itemsPerChunk;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return this.itemList.size() == 0;
    }

    /**
     *
     * @return
     */
    public int size() {
        return this.itemList.size();
    }

    /**
     *
     * @param item
     */
    public void addItem(T item) {
        this.itemList.add(item);
    }

    /**
     *
     */
    public List<T> subList(int chunkId) {
        final int fromIndex = chunkId * getItemsPerChunk();
        final int toIndex = Math.min(getItemsPerChunk() * (chunkId + 1), size());
        if (fromIndex <= toIndex) {
            return this.itemList.subList(fromIndex, toIndex);

        } else {
            throw new RuntimeException(String.format("Chunk %s not available. Valid range: %d - %d.",
                    chunkId, 0, this.numChunks));
        }
    }
}
