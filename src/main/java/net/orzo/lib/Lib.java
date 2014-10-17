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
package net.orzo.lib;

import java.io.IOException;

import net.orzo.data.Templating;
import net.orzo.data.graphics.GreyscalePicture;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miscellaneous utilities and backend-related functionality for the JavaScript
 * environment. Please note that this object is not state-less. Some operations
 * create persistent objects which are reused. This diminishes the need for
 * global objects within user scripts (e.g. you can create the same chunked file
 * reader multiple times within your dataChunks() function and it is still the
 * same object).
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * @todo refactor this (too many unrelated functions)
 */
public class Lib {

	private final ScriptableObject jsScope;

	public final Files files;

	public final Strings strings;

	public final DataStructures dataStructures;

	public final Templating templating;

	@SuppressWarnings(value = { "unused" })
	private static final Logger LOG = LoggerFactory.getLogger("user_script");

	/**
	 * 
	 * @param jsScope
	 */
	public Lib(ScriptableObject jsScope) {
		this.jsScope = jsScope;
		this.files = new Files(this.jsScope);
		this.strings = new Strings(this.jsScope);
		this.dataStructures = new DataStructures(this.jsScope);
		this.templating = new Templating();
	}

	/**
	 * Returns a number of available processors. Please note that Intel CPUs
	 * with hyper-threading report twice as high as is actual number of physical
	 * cores.
	 */
	public int numOfProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * Measures the execution time of the provided function. Please note that in
	 * case of asynchronous code you may not obtain the value you have been
	 * expecting.
	 * 
	 * @param function
	 * @return time in milliseconds
	 */
	public long measureTime(Function function) {
		long startTime = System.currentTimeMillis();
		function.call(Context.getCurrentContext(), this.jsScope, null,
				new Object[] {});
		return System.currentTimeMillis() - startTime;
	}

	/**
	 * 
	 * @param val
	 * @return
	 */
	public boolean isNumber(Object val) {
		return val instanceof Number;
	}

	/**
	 * 
	 * @param val
	 * @return
	 */
	public double doubleVal(Object val) {
		return (Double) val;
	}

	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public GreyscalePicture loadImage(String path) throws IOException {
		return GreyscalePicture.load(path);
	}

}
