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
package net.orzo;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jdk.nashorn.internal.runtime.ScriptFunction;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class FinalResults {

	/**
	 * To make Nashorn expose FinalResults' 'sorted' property (which is an
	 * anonymous class) a public interface must be defined.
	 */
	public interface SortedResults {
		void each(ScriptFunction fn);
	}

	/**
	 * 
	 */
	private final IntermediateResults results;

	/**
	 * This object makes JavaScript API nicer (but maybe less concise). It
	 * allows using sorted results by calling result.sorted.each(fn) instead of
	 * result(true, fn) from older Orzo.js versions.
	 */
	public SortedResults sorted = new SortedResults() {
		public void each(ScriptFunction fn) {
			FinalResults.this.each(fn, true);
		}
	};

	/**
	 * 
	 * @param context
	 * @param scope
	 */
	public FinalResults(IntermediateResults results) {
		this.results = results;
	}

	/**
	 * 
	 * @param fn
	 */
	public void each(ScriptFunction fn) {
		each(fn, false);
	}

	/**
	 * A simple utility allowing iterating over all the results. Passed
	 * <i>fn</i> argument is expected to be a JavaScript callback with signature
	 * function(key, values)
	 */
	private void each(ScriptFunction fn, boolean sortKeys) {
		MethodHandle mh;

		try {
			if (sortKeys) {
				for (Object key : sortedKeys()) {
					mh = fn.getBoundInvokeHandle(null); // TODO scope???
					mh.invoke(key, this.results.getData().get(key)); // TODO
																		// wrapping???
				}

			} else {
				for (Object key : this.results.getData().keySet()) {
					mh = fn.getBoundInvokeHandle(null); // TODO scope???
					mh.invoke(key, this.results.getData().get(key)); // TODO
																		// wrapping???
				}

			}

		} catch (Throwable ex) {
			throw new RuntimeException(ex); // TODO more specific exception???
		}
	}

	/**
	 * Returns a result list identified by a key
	 * 
	 * @param key
	 *            a result entry key
	 */
	public Object get(String key) {
		return this.results.getData().get(key); // TODO wrapping???
	}

	/**
	 * 
	 */
	public Object contains(String key) {
		return this.results.getData().containsKey(key); // TODO wrapping???
	}

	/**
	 * 
	 * @return
	 */
	private List<Object> sortedKeys() {
		List<Object> keys = new ArrayList<Object>(this.results.keys());

		Comparator<Object> cmp = new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		};
		Collections.sort(keys, cmp);
		return keys;
	}

	/**
	 * Returns a list of alphabetically sorted reduce-emitted keys.
	 */
	public Object keys() {
		return sortedKeys().toArray(); // TODO wrapping???
	}
}
