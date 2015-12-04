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
package net.orzo.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;


public class FilesTest {

    @Test
    public void testFileReader() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.fileReader("./test-data/text-file.txt");
        int i = 0;
        while (f.hasNext()) {
            Assert.assertEquals(f.next(), String.format("this is line %d", i));
            i++;
        }
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testFileReaderNonExistingFile() throws IOException {
        Files files = new Files();
        files.fileReader("./test-data/text-file___zzz.txt");
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testFileReaderPrematureCloseNext() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.fileReader("./test-data/text-file.txt");
        f.next();
        f.close();
        f.next();
    }

    @Test
    public void testFileReaderPrematureCloseHasNext() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.fileReader("./test-data/text-file.txt");
        f.next();
        f.close();
        Assert.assertFalse(f.hasNext());
    }

    @Test
    public void testGzipFileReader() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.gzipFileReader("./test-data/lines.txt.gz");
        int i = 0;
        while (f.hasNext()) {
            Assert.assertEquals(f.next(), String.format("line %d", i));
            i++;
        }
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testGzipFileReaderNonExistingFile() throws IOException {
        Files files = new Files();
        files.gzipFileReader("./test-data/text-file___zzz.txt");
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testGzipFileReaderPrematureCloseNext() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.gzipFileReader("./test-data/lines.txt.gz");
        f.next();
        f.close();
        f.next();
    }

    @Test
    public void testGzipFileReaderPrematureCloseHasNext() throws IOException {
        Files files = new Files();
        FileIterator<Object> f = files.gzipFileReader("./test-data/lines.txt.gz");
        f.next();
        f.close();
        Assert.assertFalse(f.hasNext());
    }


}
