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

import org.apache.commons.codec.digest.DigestUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class Strings {

	private final ScriptableObject jsScope;

	/**
	 * 
	 * @param jsScope
	 */
	public Strings(ScriptableObject jsScope) {
		this.jsScope = jsScope;
	}

	/**
	 * Prints a string to the standard output.
	 */
	public void print(Object o) {
		System.out.println(o);
	}

	/**
	 * 
	 * @param s
	 * @param args
	 */
	public void printf(String s, Object... args) {
		System.out.printf(s, args);
	}

	/**
	 * 
	 * @param s
	 * @param args
	 * @return
	 */
	public Object sprintf(String s, Object... args) {
		return Context.javaToJS(String.format(s, args), this.jsScope);
	}

	/**
	 * 
	 */
	public Object md5(String value) {
		return DigestUtils.md5Hex(value);
	}

}
