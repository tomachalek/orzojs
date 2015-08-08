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
package net.orzo.lib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import net.orzo.data.DirectoryReader;
import net.orzo.data.FilePairGenerator;
import net.orzo.data.FilePartReaderFactory;
import net.orzo.data.TwoGroupFilePairGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a library of files-related functions available to JavaScript
 * environment.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Files {

	private static final Logger LOG = LoggerFactory.getLogger(Files.class);

	/**
	 * Obtains an iterator which reads provided file (specified by path) line by
	 * line. Iterator can be accessed by a classic method pair <i>hasNext()</li>
	 * and <i>next()</i>.
	 */
	public FileIterator<Object> fileReader(final String path) throws IOException {
		final LineIterator itr = FileUtils
				.lineIterator(new File(path), "UTF-8");
		return new FileIterator<Object>() {

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public Object next() {
				return itr.nextLine(); // TODO wrapping???
			}

			@Override
			public void remove() {
				itr.remove();
			}

			public void close() {
				itr.close();
			}
		
			public String getPath() {
				if (File.separator == "/") {
					return path;

				} else {
					return path.replace(File.separator, "/");
				}
			}
		};
	}

	/**
	 * Creates a factory to generate one or more readers for a (typically large)
	 * file where each reader reads only part of the file.
	 * 
	 * @param path
	 *            path to the file we want to read
	 * @param numReaders
	 *            number of readers we want to apply to read whole file
	 * @param chunkSize
	 * @param startLine
	 * @see FilePartReaderFactory
	 */
	public synchronized FilePartReaderFactory filePartReaderFactory(
			String path, int numReaders, Integer chunkSize, Integer startLine) {
		return new FilePartReaderFactory(new File(path), numReaders, chunkSize,
				startLine);
	}

	/**
	 * Scans recursively a directory and creates numChunks iterators over these
	 * files.
	 * 
	 * @param pathList
	 *            list of directories to start search in
	 * @param numChunks
	 * @param filter
	 *            a regular expression to specify accepted files
	 * @return
	 */
	public DirectoryReader directoryReader(String[] pathList, int numChunks,
			String filter) {
		return new DirectoryReader(pathList, numChunks, filter);
	}

	/**
	 * 
	 * @param pathList
	 * @param numChunks
	 * @param filter
	 * @return
	 */
	public FilePairGenerator filePairGenerator(String[] pathList,
			int numChunks, String filter) {
		return new FilePairGenerator(pathList, numChunks, filter);
	}
	
	/**
	 * 
	 * @param pathList1
	 * @param pathList2
	 * @param numChunks
	 * @param filter
	 * @return
	 */
	public TwoGroupFilePairGenerator twoGroupFilePairGenerator(String[] pathList1,
			String[] pathList2, int numChunks, String filter) {
		return new TwoGroupFilePairGenerator(pathList1, pathList2, numChunks, filter);
	}

	/**
	 * Saves a string to a file.
	 * 
	 * @param path
	 * @param s
	 *            string to be saved
	 * @return true on success else false
	 */
	public boolean saveText(String path, String s) {
		FileWriter writer = null;
		BufferedWriter bWriter = null;

		try {
			writer = new FileWriter(new File(path));
			bWriter = new BufferedWriter(writer);
			bWriter.write(s);

		} catch (IOException ex) {
			LOG.error(ex.getMessage());

		} finally {
			if (bWriter != null) {
				try {
					bWriter.close();
				} catch (IOException ex2) {
					LOG.error(ex2.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public String readText(String path) {
		String ans = null;

		try {
			ans = FileUtils.readFileToString(new File(path),
					StandardCharsets.UTF_8);

		} catch (IOException ex) {
			LOG.error(ex.getMessage());

		} catch (UnsupportedCharsetException ex) {
			LOG.error(ex.getMessage());
		}
		return ans;
	}

	/**
	 * Creates a buffered file writer.
	 */
	public BufferedWriter createTextFileWriter(String path) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				path), "UTF-8"));
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void cleanDirectory(String path) throws IOException {
		FileUtils.cleanDirectory(new File(path));
	}
}
