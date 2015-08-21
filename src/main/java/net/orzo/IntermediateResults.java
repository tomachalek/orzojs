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
package net.orzo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents intermediate results of a single worker thread. These results are
 * merged before the <i>reduce</i> function is applied. It basically wraps
 * {@link Multimap} object and provides only required methods.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class IntermediateResults {

	/**
	 * 
	 */
	private final ListMultimap<Object, Object> data;

	/**
	 * 
	 */
	@Override
	public String toString() {
		return this.data.toString();
	}

	/**
	 * 
	 */
	public IntermediateResults() {
		this.data = ArrayListMultimap.create();
	}

	/**
	 * Adds new value with specified key
	 * 
	 * @param key
	 * @param value
	 *            any value as required by user's script
	 * @throws NullPointerException
	 *             if a null key is used
	 */
	public void add(Object key, Object value) {
		if (key == null) {
			throw new NullPointerException("Cannot add null key");
		}
		this.data.put(key, value);
	}

	public void addMultiple(Object key, List<?> values) {
		this.data.putAll(key, values);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<?> remove(Object key) {
		return new ArrayList<Object>(this.data.removeAll(key));
	}

	/**
	 * Returns set of all keys (i.e. values are unique)
	 */
	public Set<Object> keys() {
		return this.data.keySet();
	}

	/**
	 * 
	 * @return
	 */
	public int numKeys() {
		return this.data.keySet().size();
	}

	/**
	 * Returns all results attached to the specified key
	 */
	public List<Object> values(Object key) {
		return this.data.get(key);
	}

	/**
	 * 
	 */
	public Multimap<Object, Object> getData() {
		return this.data;
	}

	/**
	 * Adds all keys and values from another {@link IntermediateResults}
	 * instance (just like putAll() in case of maps).
	 * 
	 * @throws {@link IllegalArgumentException}
	 */
	public synchronized void addAll(IntermediateResults another) {
		if (another == null) { // this.data.putAll would do this too but for
								// sure...
			throw new IllegalArgumentException(
					"Cannot add null IntermediateResults");

		} else if (another == this) {
			throw new IllegalArgumentException(
					"Cannot add self as a result to merge");
		}
		this.data.putAll(another.data);
	}

}
