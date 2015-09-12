/*
 * Copyright (C) 2015 Tomas Machalek
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

import java.lang.invoke.MethodHandle;
import java.util.Comparator;

import jdk.nashorn.internal.runtime.ScriptFunction;

/**
 * A comparator which is able (in a sense) to handle errors in custom
 * JavaScript-defined compare function. A possible errors are suppressed
 * (they must be) but the last error (if any) can be obtained.
 *  
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@SuppressWarnings("restriction")
public class ErrorHandlingComparator implements Comparator<Object> {

	private Throwable err;
	
	private final ScriptFunction cmp;
	
	public ErrorHandlingComparator(ScriptFunction cmp) {
		this.cmp = cmp;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		MethodHandle mh = cmp.getBoundInvokeHandle(null);
		try {
			return (int)mh.invoke(o1, o2);
		} catch (Throwable e) {
			err = e;
		}
		return -1;
	}
	
	public Throwable lastError() {
		return this.err;
	}

}
