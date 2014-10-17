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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Provides functions for {@link MapWorker} and {@link ReduceWorker} instances'
 * JavaScript environments. Although each worker has its own JS environment,
 * there is only one instance shared across all {@link MapWorker}s which means
 * that this object should be stateless and thread-safe.
 * 
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * @see ScriptGlobalFuncHandler
 */
public class MultiThreadPhaseHandler implements ScriptGlobalFuncHandler {

	/**
	 * 
	 */
	private Function applyItemsFunction;

	/**
	 * 
	 */
	private Function dataChunksFunction;

	/**
	 * 
	 */
	private Function mapFunction;

	/**
	 * 
	 */
	private Function reduceFunction;

	/**
	 * 
	 */
	public MultiThreadPhaseHandler() {
	}

	/**
	 * 
	 */
	@Override
	public void dataChunks(int numChunks, Function function) {
		this.dataChunksFunction = function;
	}

	/**
	 * 
	 */
	@Override
	public void applyItems(Function function) {
		this.applyItemsFunction = function;
	}

	/**
	 * 
	 */
	@Override
	public void map(Function function) {
		this.mapFunction = function;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reduce(int numWorkers, Function function) {
		this.reduceFunction = function;
	}

	/**
	 * 
	 */
	@Override
	public void finish(Function function) {
		// does nothing
	}

	/**
	 * 
	 * @param cx
	 * @param scope
	 * @param thisObj
	 * @param args
	 */
	public void callApplyItems(Context cx, Scriptable scope,
			Scriptable thisObj, Object[] args) {
		this.applyItemsFunction.call(cx, scope, thisObj, args);
	}

	/**
	 * 
	 * @param cx
	 * @param scope
	 * @param thisObj
	 * @param args
	 * @return
	 */
	public Object callDataChunks(Context cx, Scriptable scope,
			Scriptable thisObj, Object[] args) {
		return this.dataChunksFunction.call(cx, scope, thisObj, args);
	}

	/**
	 * 
	 * @return registered function for the <i>map</i> operation
	 */
	public Function getMapFunction() {
		return this.mapFunction;
	}

	/**
	 * Calls the <i>reduce</i> function as provided by the user
	 * 
	 * @param cx
	 * @param scope
	 * @param thisObj
	 * @param args
	 * @return
	 */
	public Object callReduceFunction(Context cx, Scriptable scope,
			Scriptable thisObj, Object[] args) {
		return this.reduceFunction.call(cx, scope, thisObj, args);
	}

}
