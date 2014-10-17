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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

public class FinalResults {

	/**
	 * 
	 */
	private final IntermediateResults results;

	/**
	 * 
	 */
	private final Context context;

	/**
	 * 
	 */
	private final ScriptableObject scope;

	/**
	 * 
	 * @param context
	 * @param scope
	 */
	public FinalResults(IntermediateResults results, Context context,
			ScriptableObject scope) {
		this.results = results;
		this.context = context;
		this.scope = scope;
	}

	/**
	 * 
	 * @param fn
	 */
	public void each(Function fn) {
		each(false, fn);
	}

	/**
	 * A simple utility allowing iterating over all the results. Passed
	 * <i>fn</i> argument is expected to be a JavaScript callback with signature
	 * function(key, values)
	 */
	public void each(boolean sortKeys, Function fn) {
		if (sortKeys) {
			for (Object key : sortedKeys()) {
				fn.call(this.context,
						this.scope,
						this.scope,
						new Object[] {
								key,
								Context.javaToJS(this.results.getData()
										.get(key).toArray(), this.scope) });
			}

		} else {
			for (Object key : this.results.getData().keySet()) {
				fn.call(this.context,
						this.scope,
						this.scope,
						new Object[] {
								key,
								Context.javaToJS(this.results.getData()
										.get(key).toArray(), this.scope) });
			}

		}
	}

	/**
	 * Returns a result list identified by a key
	 * 
	 * @param key
	 *            a result entry key
	 */
	public Object get(String key) {
		return Context.javaToJS(this.results.getData().get(key), this.scope);
	}

	/**
	 * 
	 */
	public Object contains(String key) {
		return Context.javaToJS(this.results.getData().containsKey(key),
				this.scope);
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
		return Context.javaToJS(sortedKeys().toArray(), this.scope);
	}
}
