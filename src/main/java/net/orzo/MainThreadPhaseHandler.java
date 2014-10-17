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
 * Performs actions processed in a single-thread mode (e.g. the <i>reduce</i>
 * action) by {@link Calculation} instance.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class MainThreadPhaseHandler implements ScriptGlobalFuncHandler {

	private int numChunks;

	private Function finishFunction;

	/**
	 * 
	 */
	private int numReduceWorkers;

	/**
	 * 
	 */
	public MainThreadPhaseHandler() {

	}

	/**
	 * Registers number of chunks. Parameter <i>function</i> is ignored here (
	 * {@link MultiThreadPhaseHandler} uses that).
	 */
	@Override
	public void dataChunks(int numChunks, Function function) {
		this.numChunks = numChunks;
	}

	/**
	 * Does nothing in case of this type
	 */
	@Override
	public void applyItems(Function function) {
		// does nothing
	}

	/**
	 * Does nothing in case of this type
	 */
	@Override
	public void map(Function function) {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reduce(int numWorkers, Function function) {
		if (numWorkers > 0) {
			this.numReduceWorkers = numWorkers;

		} else {
			this.numReduceWorkers = this.numChunks;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish(Function function) {
		this.finishFunction = function;
	}

	/**
	 * Returns total number of registered data chunks
	 * 
	 * @return
	 */
	public int getNumChunks() {
		return this.numChunks;
	}

	/**
	 * Calls the <i>finish</i> function as defined by the user
	 * 
	 * @param cx
	 * @param scope
	 * @param thisObj
	 * @param args
	 * @return
	 */
	public Object callFinishFunction(Context cx, Scriptable scope,
			Scriptable thisObj, Object[] args) {
		return this.finishFunction.call(cx, scope, thisObj, args);
	}

	/**
	 * 
	 */
	public int getNumReduceWorkers() {
		return this.numReduceWorkers;
	}

}
