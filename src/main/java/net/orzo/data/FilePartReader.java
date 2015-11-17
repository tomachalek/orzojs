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
package net.orzo.data;

import java.util.Iterator;

import org.apache.commons.io.LineIterator;

/**
 * This class iterates over chunks of a file using an internal
 * {@link LineIterator}. The chunks of the single reader typically do not cover
 * all the data (in such case a standard file reader would be better and simpler
 * to use) but only a fraction of it. It is best suited to situations where a
 * number of {@link FilePartReader} instances read a (large) file.
 * 
 * Example: we can define 3 {@link FilePartReader} instances with properly
 * initialized line iterators (an initial offset must be set) then the first
 * iterator will read 0th, 3rd, 6th,..., chunks:
 * 
 * <pre>
 * xxx --- --- xxx --- --- xxx --- ---
 * </pre>
 * 
 * the second iterator will read 1st, 4th, 7th, ... chunks:
 * 
 * <pre>
 * --- xxx --- --- xxx --- --- xxx ---
 * </pre>
 * 
 * and the third:
 * 
 * <pre>
 * --- --- xxx --- --- xxx --- --- xxx
 * </pre>
 * 
 * The easiest way to instantiate this class is via
 * {@link FilePartReaderFactory} which provides proper iterator initalization
 * and can also provide an estimation of a chunk size to offer decent
 * performance.
 * 
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class FilePartReader implements Iterator<String> {

    /**
     *
     */
    private final PositionAwareLineIterator lineIterator;

    /**
     *
     */
    private final int numGroups;

    /**
     *
     */
    private final int linesPerGroup;

    /**
     *
     */
    private int currLine;

    /**
     * @param lineIterator
     *            a source iterator; please note that initial offset must be
     *            already set (i.e. this class does not initialize it for you).
     * @param numGroups
     *            how many iterators will be defined to read the file
     * @param linesPerGroup
     *            how many individual lines will a single group contain
     */
    public FilePartReader(PositionAwareLineIterator lineIterator,
            int numGroups, int linesPerGroup) throws IllegalArgumentException {
        this.lineIterator = lineIterator;
        this.numGroups = numGroups;
        this.linesPerGroup = linesPerGroup;
        this.currLine = 0;
    }

    /**
     *
     */
    @Override
    public boolean hasNext() {
        if (this.numGroups <= 0 || this.linesPerGroup <= 0) {
            throw new FilePartReaderMisconfiguration();
        }
        return this.lineIterator.hasNext();
    }

    /**
     * Returns the next line. Please note that in case of {@link FilePartReader}
     * the next line may be in fact many lines ahead of the previously read line
     * (see the class' documentation).
     */
    @Override
    public String next() {
        if (this.numGroups <= 0 || this.linesPerGroup <= 0) {
            throw new FilePartReaderMisconfiguration();
        }

        String ans = this.lineIterator.next();

        this.currLine++;
        if (this.currLine == this.linesPerGroup) {
            this.lineIterator.skipBy(this.linesPerGroup * (this.numGroups - 1) + 1);
            this.currLine = 0;
        }
        return ans;
    }

    /**
     * Unsupported
     */
    @Override
    public void remove() {
    }
}
