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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 * @author Tomas Machalek<tomas.machalek@gmail.com>
 * 
 */
public class DataStructures {

	private final ScriptableObject jsScope;

	/**
	 * 
	 * @param jsScope
	 */
	public DataStructures(ScriptableObject jsScope) {
		this.jsScope = jsScope;
	}

	/**
	 * Creates a native JavaScript array. It should be faster than doing this in
	 * JavaScript.
	 * 
	 * @param size
	 * @return native JavaScript array
	 */
	public Object array(int size) {
		return Context.javaToJS(new Object[size], this.jsScope);
	}

	/**
	 * Creates a native JavaScript zero-filled array. It should be faster than
	 * doing this in JavaScript.
	 * 
	 * @param size
	 * @return native JavaScript array
	 */
	public Object zeroFillArray(int size) {
		return Context.javaToJS(new double[size], this.jsScope);
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public Object numericMatrix(int width, int height) {
		return Context.javaToJS(new double[width][height], this.jsScope);
	}

	/**
	 * 
	 * @param arr
	 * @return
	 */
	public Object flattenMatrix(Object inMatrix) {
		double[][] arr = (double[][]) Context.jsToJava(inMatrix,
				double[][].class);
		double[] ans = new double[arr.length * arr[0].length];

		// this method is less then 1% slower then System.arraycopy
		// and does not depend on native implementation
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				ans[i * arr[i].length + j] = arr[i][j];
			}
		}
		return Context.javaToJS(ans, this.jsScope);
	}

	/**
	 * From a JavaScript array it creates a new one with unique occurrence of
	 * items.
	 * 
	 * @param jsArray
	 *            a JavaScript or Java array
	 * @param key
	 *            a JavaScript function to access values to be considered; null
	 *            is also possible (in such case, the value itself is used)
	 * @return a JavaScript array with unique items
	 */
	public Object uniq(Object jsArray, Function key) {
		Set<Object> set = new HashSet<Object>();
		Collection<?> origData;

		if (jsArray.getClass().isArray()) {
			origData = Arrays.asList(jsArray);

		} else {
			origData = (Collection<?>) Context.jsToJava(jsArray,
					Collection.class);
		}

		if (key == null) {
			set.addAll(origData);

		} else {
			for (Object item : origData) {
				set.add(key.call(Context.enter(), this.jsScope, null,
						new Object[] { item }));
			}
		}
		return Context.javaToJS(set, this.jsScope);
	}
}
