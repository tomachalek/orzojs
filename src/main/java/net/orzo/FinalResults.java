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
import java.util.Collection;
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
	 * 
	 * @param context
	 * @param scope
	 */
	public FinalResults(IntermediateResults results) {
		this.results = results;
	}


	/**
	 * Returns a result list identified by a key
	 * 
	 * @param key
	 *            a result entry key
	 */
	public Collection<Object> get(String key) {
		return this.results.getData().get(key);
	}

	/**
	 * 
	 */
	public boolean contains(String key) {
		return this.results.getData().containsKey(key);
	}

	/**
	 * 
	 * @param sorted
	 * @return
	 */
	public List<Object> keys(boolean sorted) {
		if (sorted) {
			return sortedKeys();

		} else {
			return new ArrayList<Object>(this.results.keys());
		}
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
}
