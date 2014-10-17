/*
 * Copyright (c) 2013 Tomas Machalek
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

package net.orzo.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.orzo.tools.ResourceLoader;

/**
 * Represents general source code with some identifier/name and contents. The
 * class itself does not provide any methods to determine whether the contents
 * is a "script", even a text (i.e. it is possible to initialize an instance
 * with binary data too and no error occurs until it is used somewhere where a
 * text is expected).
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class SourceCode {

	/**
	 * 
	 */
	private final String id;

	/**
	 * 
	 */
	private final String contents;

	/**
	 * @param id
	 *            name of the script
	 * @param contents
	 *            source code of the script
	 */
	public SourceCode(String id, String contents) {
		this.id = id != null ? id : "unnamed";
		this.contents = contents != null ? contents : "";
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return String.format("<%s>: %s", this.id, this.contents);
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Creates source code object using contents of provided file.
	 * 
	 * @param f
	 *            text file containing source code
	 * @return source code containing contents of file f
	 */
	public static SourceCode fromFile(File f) throws IOException {
		FileReader freader = new FileReader(f);
		BufferedReader bReader = new BufferedReader(freader);
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = bReader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		bReader.close();
		return new SourceCode(f.getName(), buffer.toString());
	}

	/**
	 * Creates source code from a Java resource identified by its absolute path
	 * (e.g. net/orzo/bootstrap.js)
	 * 
	 * @param res
	 * 
	 */
	public static SourceCode fromResource(String res) throws IOException {
		String id = res.substring(Math.max(0, res.lastIndexOf("/") + 1));
		String source = new ResourceLoader().getResourceAsString(res);
		if (source == null) {
			throw new IOException("Failed to find resource " + res);
		}
		return new SourceCode(id, source);
	}
}
