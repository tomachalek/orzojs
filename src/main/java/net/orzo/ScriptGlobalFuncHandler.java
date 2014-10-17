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

import org.mozilla.javascript.Function;

/**
 * Represents an object which is able to handle function calls in user's
 * JavaScript code. These calls actually do not perform any calculation but
 * rather register user's functions for <i>map</i>, <i>reduce</i> and other
 * actions. In case of <i>orzo</i> there are two implementations: 1) handler
 * used by {@link MapWorker} instances (i.e. in multi-threaded environment)
 * where functions like <i>map</i>, <i>applyItems</i>, <i>dataChunks</i> are
 * needed. 2) handler used by the {@link Calculation} class which controls whole
 * process.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public interface ScriptGlobalFuncHandler {

	/**
	 * Registers <i>dataChunks</i> function.
	 * 
	 * @param numChunks
	 *            total number of chunks
	 * @param function
	 *            function providing chunk by its index
	 */
	public void dataChunks(int numChunks, Function function);

	/**
	 * Registers <i>applyItems</i> function.
	 * 
	 * @param function
	 */
	public void applyItems(Function function);

	/**
	 * Registers <i>map</i> function.
	 * 
	 * @param function
	 */
	public void map(Function function);

	/**
	 * Registers <i>reduce</i> function.
	 * 
	 * @param int
	 * @param function
	 */
	public void reduce(int numWorkers, Function function);

	/**
	 * Registers <i>finish</i> function.
	 * 
	 * @param function
	 */
	public void finish(Function function);
}
