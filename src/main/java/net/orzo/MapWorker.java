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

import java.util.concurrent.Callable;

import net.orzo.scripting.EnvParams;
import net.orzo.scripting.JsEngineFacade;
import net.orzo.scripting.SourceCode;

/**
 * Handles a processing thread of a single data chunk. The result is returned as
 * {@link IntermediateResults} object.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class MapWorker implements Callable<IntermediateResults> {

	private final EnvParams envParams;

	private final JsEngineFacade jsEngine;

	private final SourceCode[] sources;

	private final MultiThreadPhaseHandler workerOps;

	private final IntermediateResults intermediateData;

	/**
	 * 
	 * @param envParams
	 * @param workerOps
	 * @param sourceCodes
	 */
	public MapWorker(EnvParams envParams, MultiThreadPhaseHandler workerOps,
			SourceCode... sourceCodes) {
		this.envParams = envParams;
		this.workerOps = workerOps;
		this.intermediateData = new IntermediateResults();
		this.jsEngine = new JsEngineFacade(envParams, this.workerOps,
				this.intermediateData);
		this.sources = sourceCodes;
	}

	/**
	 * 
	 */
	@Override
	public IntermediateResults call() throws Exception {
		this.jsEngine.beginWork();
		this.jsEngine.runCode(this.sources);

		Object dataChunk = this.workerOps.callDataChunks(
				this.jsEngine.getContext(), this.jsEngine.getScope(),
				this.jsEngine.getScope(),
				new Object[] { this.envParams.workerId });
		this.workerOps.callApplyItems(this.jsEngine.getContext(),
				this.jsEngine.getScope(), this.jsEngine.getScope(),
				new Object[] { dataChunk, this.workerOps.getMapFunction() });

		this.jsEngine.endWork();
		return this.intermediateData;
	}

}
