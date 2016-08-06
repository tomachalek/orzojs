/*
 * Copyright (C) 2016 Tomas Machalek
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


import java.io.Serializable;
import java.util.HashMap;

/**
 *
 */
public class HashTable implements Serializable {

    public static final long serialVersionUID = 100L;

    private HashMap<String, Object> data;

    HashTable(int initialCapacity) {
        this.data = new HashMap<>(initialCapacity);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public void put(String key, Object value) {
        this.data.put(key, value);
    }

    public Object remove(String key) {
        return this.data.remove(key);
    }

    public boolean hasKey(String key) {
        return this.data.containsKey(key);
    }

    public boolean hasValue(Object value) {
        return this.data.containsValue(value);
    }

    public int size() {
        return this.data.size();
    }
}
