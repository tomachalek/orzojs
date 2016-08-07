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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Using this class you can define multiple readers of a (large) line-separated
 * text file. Each reader reads only a part of the file. The part has a form of
 * scattered chunks of the same size. Please see {@link FilePartReader} for
 * details.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * @see FilePartReader
 */
public class FilePartReaderFactory {

    private final File file;

    private final int numReaders;

    private int linesPerChunk;

    private final int startFromLine;

    /**
     * * Creates a chunked file reading handler with manually set chunk size.
     *
     * @param file
     *            file to be read
     * @param numReaders
     *            how many chunks we want to define
     * @param chunkSize
     *            length of a chunk (in lines)
     */
    public FilePartReaderFactory(File file, int numReaders, Integer chunkSize,
            Integer startFromLine) {
        this.file = file;
        this.numReaders = numReaders;
        this.linesPerChunk = chunkSize != null ? chunkSize
                : estimateChunkSize();
        this.startFromLine = startFromLine;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return String
                .format("ChunkedFile [size: %d, num. of readers: %d, lines per chunk: %d]",
                        this.file.length(), this.numReaders, this.linesPerChunk);
    }

    /**
     * Returns an initial line offset for a specific reader (we assume that N
     * readers have ids [0, 1, 2,..., N-1]).
     *
     * @param readerId
     * @return line offset for selected readerId; if the file is empty then -1
     *         is returned
     */
    public int calcInitOffset(int readerId) throws IOException {
        if (this.file.length() > 0) {
            return Math.round(readerId * this.linesPerChunk + this.startFromLine);
        }
        return -1;
    }

    /**
     * Returns a file-part reader.
     *
     * @param readerId
     * @return list of lines from the respective chunk or null if no such chunk
     *         exists
     * @throws FileNotFoundException
     * @throws IllegalArgumentException in case the readerId is
     *         incorrect
     */
    public FilePartReader createInstance(int readerId)
            throws FilePartReaderFactoryException, FileNotFoundException {
        PositionAwareLineIterator itr;
        int offset;

        if (!this.file.exists()) {
            throw new FileNotFoundException(String.format(
                    "File %s does not exist.", this.file));

        } else if (readerId >= this.numReaders || readerId < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "A reader id must be from interval [%d,  %d], obtained: %d",
                            0, this.numReaders - 1, readerId));
        }

        try {
            offset = calcInitOffset(readerId);
            itr = PositionAwareLineIterator.create(this.file, "UTF-8");
            itr.skipTo(offset);

        } catch (IOException ex) {
            throw new FilePartReaderFactoryException(String.format(
                    "Failed to get chunk iterator [%s]", readerId), ex);
        }
        return new FilePartReader(itr, this.numReaders, this.linesPerChunk);
    }

    /**
     *
     */
    private int estimateChunkSize() {
        return 1000; // TODO
    }
}
