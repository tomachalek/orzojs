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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * An iterator with ability to skip lines.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class PositionAwareLineIterator implements Iterator<String> {

    private final LineIterator lineIterator;

    private long currLine = -1;

    /**
     *
     * @param path
     * @param encoding
     */
    public static PositionAwareLineIterator create(String path, String encoding)
            throws IOException {
        return new PositionAwareLineIterator(FileUtils.lineIterator(new File(
                path), encoding));
    }

    /**
     *
     * @param encoding
     * @return
     * @throws IOException
     */
    public static PositionAwareLineIterator create(File file, String encoding)
            throws IOException {
        return new PositionAwareLineIterator(FileUtils.lineIterator(file,
                encoding));
    }

    /**
     *
     */
    public static PositionAwareLineIterator create(File file)
            throws IOException {
        return new PositionAwareLineIterator(FileUtils.lineIterator(file,
                "UTF-8"));
    }

    /**
     *
     */
    public static PositionAwareLineIterator create(String path)
            throws IOException {
        return new PositionAwareLineIterator(FileUtils.lineIterator(new File(
                path), "UTF-8"));
    }

    /**
     *
     * @param lineIterator
     */
    public PositionAwareLineIterator(LineIterator lineIterator) {
        this.lineIterator = lineIterator;
    }

    @Override
    public boolean hasNext() {
        return this.lineIterator.hasNext();
    }

    @Override
    public String next() {
        this.currLine++;
        return this.lineIterator.next();
    }

    /**
     * Unsupported operation (does nothing)
     */
    @Override
    public void remove() {
    }

    /**
     *
     * @param line
     * @throws IllegalArgumentException
     *             in case a negative value is passed
     */
    public void skipTo(long line) {
        if (line < 0) {
            throw new IllegalArgumentException("Cannot skip to a negative line");
        }
        while (this.currLine < line - 1 && hasNext()) {
            next();
        }
    }

    /**
     *
     * @param lines
     * @throws IllegalArgumentException
     *             in case a negative value is passed
     */
    public void skipBy(long lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("Cannot skip to a negative line");
        }
        long prev = this.currLine;
        while (hasNext() && this.currLine < prev + lines - 1) {
            next();
        }
    }

    /**
     *
     * @return
     */
    public long getCurrLine() {
        return this.currLine;
    }
}
