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

import java.io.*;
import java.lang.invoke.MethodHandle;

import java.util.*;

import com.google.gson.Gson;
import jdk.nashorn.internal.runtime.ScriptFunction;

/**
 * 
 * @author Tomas Machalek<tomas.machalek@gmail.com>
 * 
 */
@SuppressWarnings("restriction")
public class DataStructures {

    /**
     * Creates a native JavaScript array. It should be faster than doing this in
     * JavaScript.
     *
     * @param size
     * @return native JavaScript array
     */
    public Object array(int size) {
        return new Object[size]; // TODO wrapping???
    }

    /**
     * Creates a native JavaScript zero-filled array. It should be faster than
     * doing this in JavaScript.
     *
     * @param size
     * @return native JavaScript array
     */
    public Object zeroFillArray(int size) {
        return new double[size]; // TODO wrapping ???
    }

    /**
     *
     */
    public Object numericMatrix(int width, int height) {
        return new double[width][height]; // TODO wrapping???
    }

    /**
     *
     */
    public Object flattenMatrix(Object inMatrix) {
        double[][] arr = (double[][]) inMatrix; // TODO wrapping ???
        double[] ans = new double[arr.length * arr[0].length];

        // this method is less then 1% slower then System.arraycopy
        // and does not depend on native implementation
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                ans[i * arr[i].length + j] = arr[i][j];
            }
        }
        return ans; // TODO wrapping???
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
    public Object uniq(Object jsArray, ScriptFunction key) {
        Set<Object> set = new HashSet<>();
        Collection<?> origData;

        if (jsArray.getClass().isArray()) {
            origData = Arrays.asList(jsArray);

        } else {
            origData = (Collection<?>) jsArray; // TODO wrapping???
        }

        try {
            if (key == null) {
                set.addAll(origData);

            } else {
                for (Object item : origData) {
                    MethodHandle mh = key.getBoundInvokeHandle(item);
                    set.add(mh.invoke(item));
                }
            }
            return set; // TODO wrapping???

        } catch (Throwable ex) {
            throw new LibException(ex);
        }
    }

    public Object hashMap(int initialCapacity) {
        return new HashTable(initialCapacity);
    }

    public void serialize(Object obj, String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(obj);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object deserialize(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return ois.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Converts an object to JSON (using Gson library)
     *
     */
    public Object toJson(Object obj) {
        return new Gson().toJson(obj);
    }
}
