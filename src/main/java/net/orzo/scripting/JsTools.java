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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;

/**
 * This is a bunch of static methods allowing retrieval of JavaScript's runtime
 * values such as arrays and objects.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class JsTools {

	/**
	 * Imports native JavaScript array into a List<...>. Method throws an
	 * 
	 * @param <T>
	 *            expected value type, all items will be casted to it
	 * @param array
	 *            input array
	 * @param type
	 *            expected value type, all items will be casted to it
	 * @return converted array or null if variable does not exist
	 * @throws IllegalArgumentException
	 *             in case of invalid type imported
	 */
	public static Object[] importArray(Object array) {
		if (array instanceof org.mozilla.javascript.UniqueTag) {
			return null;

		} else if (!(array instanceof NativeArray)) {
			throw new IllegalArgumentException(String.format(
					"Expected NativeArray, obtained %s", array.getClass()
							.getSimpleName()));
		}
		NativeArray natArray = (NativeArray) array;
		Object[] items = new Object[(int) natArray.getLength()];

		for (int i = 0; i < natArray.size(); i++) {
			Object obj = natArray.get(i);

			if (obj instanceof NativeJavaObject) {
				obj = ((NativeJavaObject) obj).unwrap();
				items[i] = obj;

			} else if (obj instanceof NativeArray) {
				items[i] = importArray(obj);

			} else if (obj instanceof NativeObject) {
				items[i] = importObject(obj);

			} else {
				items[i] = obj;
			}
		}
		return items;
	}

	/**
	 * 
	 * @param array
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> importJavaObjectArray(Object array, Class<T> type) {
		if (array instanceof org.mozilla.javascript.UniqueTag) {
			return null;

		} else if (!(array instanceof NativeArray)) {
			throw new IllegalArgumentException(String.format(
					"Expected NativeArray, obtained %s", array.getClass()
							.getSimpleName()));
		}
		NativeArray natArray = (NativeArray) array;
		List<T> items = new ArrayList<T>((int) natArray.getLength());

		for (Object obj : natArray) {
			if (!type.isAssignableFrom(obj.getClass())) {
				throw new TypeHandlingException(
						String.format(
								"Failed to add object of type %s to List<%s> container",
								obj.getClass().getSimpleName(),
								type.getSimpleName()));
			}
			items.add((T) obj);
		}
		return items;
	}

	/**
	 * 
	 */
	public static List<NativeObject> importNativeObjectArray(Object array) {
		if (array instanceof org.mozilla.javascript.UniqueTag) {
			return null;

		} else if (!(array instanceof NativeArray)) {
			throw new IllegalArgumentException(String.format(
					"Expected NativeArray, obtained %s", array.getClass()
							.getSimpleName()));
		}
		NativeArray natArray = (NativeArray) array;
		List<NativeObject> items = new ArrayList<NativeObject>(
				(int) natArray.getLength());
		for (Object obj : natArray) {
			items.add((NativeObject) obj);
		}
		return items;
	}

	/**
	 * 
	 */
	public static Double importNumber(Object value) {
		if (value instanceof Double) {
			return (Double) value;

		} else if (value instanceof Integer) {
			return ((Integer) value).doubleValue();

		} else if (value instanceof Float) {
			return ((Float) value).doubleValue();

		} else {
			return Double.NaN;
		}
	}

	/**
	 * 
	 */
	public static boolean isFunction(NativeObject object, String name) {
		Object prop = object.get(name, object);
		return "class org.mozilla.javascript.NativeFunction".equals(prop
				.getClass().getGenericSuperclass().toString());
	}

	/**
	 * Imports JavaScript object's attributes as a map of String -> Object
	 * pairs. Objects' methods are excludes from this. If there is no such
	 * object instantiated within the runtime then null is returned.
	 * 
	 * @param object
	 *            imported object
	 * @return map of String->Object pairs
	 * @IllegalArgumentException in case of invalid type imported
	 */
	public static Map<String, Object> importObject(Object object) {
		if (object instanceof org.mozilla.javascript.UniqueTag) {
			return null;

		} else if (!(object instanceof NativeObject)) {
			throw new IllegalArgumentException(String.format(
					"Expected NativeObject, obtained %s", object.getClass()
							.getSimpleName()));
		}
		NativeObject natObject = (NativeObject) object;

		Map<String, Object> ans = new HashMap<String, Object>();
		Object[] ids = natObject.getAllIds();
		for (int i = 0; i < ids.length; i++) {
			Object attr = natObject.get((String) ids[i],
					natObject.getParentScope());

			if (!attr.getClass().getGenericSuperclass().toString()
					.equals("class org.mozilla.javascript.NativeFunction")) {
				if (attr instanceof NativeArray) {
					ans.put(ids[i].toString(), importArray(attr));

				} else if (attr instanceof NativeObject) {
					ans.put(ids[i].toString(), importObject(attr));

				} else {
					ans.put(ids[i].toString(), attr);
				}
			}
		}
		return ans;
	}
}
