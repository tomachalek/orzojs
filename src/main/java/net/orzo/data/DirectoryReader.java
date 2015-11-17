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

import static net.orzo.Util.normalizePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a list of files in a recursive searched directory and returns
 * iterators to respective chunks of this list. E.g. 100 file list and 2-chunks
 * leads to two iterators - one for items from 0 to 49 and one for items from
 * 50-99.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class DirectoryReader {

    private final File[] rootList;

    private final int numChunks;

    private final String filterRegexp;

    private ArrayList<String> fileList = null;

    private int filesPerChunk;

    /**
     *
     * @param pathList
     *            a list of directories to be searched
     * @param numChunks
     * @param filter
     *            a regular expression to filter accepted files, null is also ok
     */
    public DirectoryReader(String[] pathList, int numChunks, String filter) {
        this.rootList = new File[pathList.length];
        for (int i = 0; i < pathList.length; i++) {
            this.rootList[i] = new File(pathList[i]);
        }
        this.numChunks = numChunks;
        this.filterRegexp = filter != null ? filter : ".+";
    }

    /**
     *
     * @param idx
     *            iterator id (valid values are from interval [0, numChunks-1])
     */
    public Iterator<String> getIterator(int idx) {
        int rightIdx;

        if (this.fileList == null) {
            this.fileList = fetchFileList();
            this.filesPerChunk = (int) Math.ceil((float)this.fileList.size()
                    / this.numChunks);
        }
        rightIdx = Math.min(this.filesPerChunk * (idx + 1),
                this.fileList.size());

        if (idx * this.filesPerChunk < rightIdx) {
            return this.fileList.subList(idx * this.filesPerChunk, rightIdx).iterator();

        } else {
            return Collections.emptyIterator();
        }
    }

    /**
     * Lists all files in a specified directory (including contents of
     * subdirectories)
     *
     * @return
     */
    private ArrayList<String> fetchFileList() {
        ArrayList<String> fileList = new ArrayList<>();
        for (File f : this.rootList) {
            getDirectoryList(f, fileList);
        }
        Collections.sort(fileList);
        return fileList;
    }


    /**
     * Recursive method which searches for files/directories on a specified path
     *
     * @param rootDir
     * @param foundFiles
     *            results are stored here
     */
    private void getDirectoryList(File rootDir, ArrayList<String> foundFiles) {
        File[] list = rootDir.listFiles();
        Matcher matcher;

        if (list != null) {
            Pattern filter = Pattern.compile(this.filterRegexp);

            for (File f : list) {
                if (f.isDirectory()) {
                    getDirectoryList(f, foundFiles);

                } else if (f.isFile()) {
                    matcher = filter.matcher(f.getName());
                    if (matcher.find()) {
                        foundFiles.add(normalizePath(f.getAbsolutePath()));
                    }
                }
            }
        }
    }
}
