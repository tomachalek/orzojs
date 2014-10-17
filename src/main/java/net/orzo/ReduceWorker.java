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

import java.util.concurrent.Callable;

import net.orzo.scripting.EnvParams;
import net.orzo.scripting.JsEngineFacade;
import net.orzo.scripting.SourceCode;

import org.mozilla.javascript.Context;

/**
 * Handles a processing thread of the REDUCE phase.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class ReduceWorker implements Callable<IntermediateResults> {

	private final EnvParams envParams;

	private final SourceCode[] sources;

	private final MultiThreadPhaseHandler workerOps;

	private final IntermediateResults mapResults;

	private final IntermediateResults resultData;

	private final JsEngineFacade jsEngine;

	/**
	 * 
	 * @param envParams
	 * @param workerOps
	 * @param sourceCodes
	 */
	public ReduceWorker(EnvParams envParams, MultiThreadPhaseHandler workerOps,
			IntermediateResults mapResults, SourceCode... sourceCodes) {
		this.envParams = envParams;
		this.workerOps = workerOps;
		this.mapResults = mapResults;
		this.resultData = new IntermediateResults();
		this.jsEngine = new JsEngineFacade(this.envParams, this.workerOps,
				this.resultData);
		this.sources = sourceCodes;
	}

	@Override
	public IntermediateResults call() throws Exception {
		this.jsEngine.beginWork();
		this.jsEngine.runCode(this.sources);

		for (Object key : this.mapResults.keys()) {
			this.workerOps.callReduceFunction(
					this.jsEngine.getContext(),
					this.jsEngine.getScope(),
					this.jsEngine.getScope(),
					new Object[] {
							key,
							Context.javaToJS(this.mapResults.values(key),
									this.jsEngine.getScope()) });
		}
		return this.resultData;
	}

}