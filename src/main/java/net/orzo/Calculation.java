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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.orzo.scripting.EnvParams;
import net.orzo.scripting.JsEngineFacade;
import net.orzo.scripting.ScriptProcessingException;
import net.orzo.scripting.SourceCode;

import org.mozilla.javascript.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs map-reduce calculation.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class Calculation {

	/**
	 * 
	 */
	public static final int CALC_PHASE_MAP = 0;

	/**
	 * 
	 */
	public static final int CALC_PHASE_REDUCE = 1;

	/**
	 * 
	 */
	public static final int CALC_PHASE_OTHER = 2;

	/**
	 * 
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(Calculation.class);

	/**
	 * 
	 */
	private final MainThreadPhaseHandler mainScopeOps;

	/**
	 * Contains pack of useful functions.
	 */
	private final SourceCode datalibScript;

	/**
	 * This script is needed to connect the JavaScript environment with Java
	 * back end properly.
	 */
	private final SourceCode bootstrapScript;

	/**
	 * User's script
	 */
	private final SourceCode userScript;

	/**
	 * 
	 */
	private final List<String> modulesPaths;

	/**
	 * 
	 */
	private final String[] inputValues;

	/**
	 * 
	 * @param mainThreadOps
	 * @param bootstrapScript
	 * @param userScript
	 */
	public Calculation(MainThreadPhaseHandler mainScopeOps,
			CalculationParams params) {
		this.mainScopeOps = mainScopeOps;
		this.bootstrapScript = params.bootstrapScript;
		this.datalibScript = params.datalibScript;
		this.userScript = params.userScript;
		this.inputValues = params.inputValues;
		this.modulesPaths = new ArrayList<String>();
		this.modulesPaths.add(params.modulesPath);
		if (params.optionalModulesPath != null) {
			this.modulesPaths.add(params.optionalModulesPath);
		}
	}

	/**
	 * Starts and controls the calculation.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ScriptProcessingException
	 */
	public void run() throws ScriptProcessingException {
		IntermediateResults mapResults;
		IntermediateResults reduceResults = null;
		long startTime = System.currentTimeMillis();
		Map<String, Object> calcInfo = new HashMap<String, Object>();
		calcInfo.put("datetime", getCurrentDate());

		mapResults = runMap();

		if (!mapResults.hasErrors()) {
			reduceResults = runReduce(mapResults);
		}

		if (reduceResults != null && !reduceResults.hasErrors()) {
			EnvParams envParams = new EnvParams();
			envParams.calculationPhase = CALC_PHASE_OTHER;
			envParams.inputArgs = this.inputValues;
			envParams.modulesPaths = this.modulesPaths;

			JsEngineFacade jse = new JsEngineFacade(envParams,
					this.mainScopeOps);
			jse.beginWork();

			FinalResults fr = new FinalResults(reduceResults, jse.getContext(),
					jse.getScope());
			calcInfo.put("duration",
					(System.currentTimeMillis() - startTime) / 1000.0);

			System.err.printf("\n\nFinished in %01.3f seconds.\n",
					calcInfo.get("duration"));

			this.mainScopeOps.callFinishFunction(jse.getContext(),
					jse.getScope(), jse.getScope(),
					new Object[] { Context.javaToJS(fr, jse.getScope()),
							Context.javaToJS(calcInfo, jse.getScope()) });
			jse.endWork();

		} else {
			System.err.println("Failed to execute the script.");

			if (mapResults.hasErrors()) {
				System.err.println("Map errors:\n");
				for (Exception e : mapResults.getErrors()) {
					System.err.println(e);
				}
			}

			if (reduceResults == null) {
				System.err.println("Reduce phase not run.");

			} else if (reduceResults.hasErrors()) {
				System.err.print("Reduce errors:\n");
				for (Exception e : reduceResults.getErrors()) {
					System.err.println(e);
				}
			}
		}
	}

	/**
	 * Runs the MAP phase. Input is given by user's script,output is
	 * {@link IntermediateResults} which stores respective keys and lists of
	 * values.
	 * 
	 * @return key => [value1, value2,..., valueN] for all emitted keys and
	 *         values
	 */
	private IntermediateResults runMap() throws ScriptProcessingException {
		ExecutorService executor;
		List<Future<IntermediateResults>> threadList = new ArrayList<Future<IntermediateResults>>();
		Callable<IntermediateResults> worker;
		IntermediateResults mapResults = new IntermediateResults();

		EnvParams envParams = new EnvParams();
		envParams.calculationPhase = CalculationPhase.OTHER;
		envParams.inputArgs = this.inputValues;
		envParams.modulesPaths = this.modulesPaths;

		JsEngineFacade jse = new JsEngineFacade(envParams, this.mainScopeOps);
		jse.beginWork();
		jse.runCode(new SourceCode[] { this.bootstrapScript,
				this.datalibScript, this.userScript });

		executor = Executors.newFixedThreadPool(mainScopeOps.getNumChunks());
		EnvParams workerEnvParams;
		for (int i = 0; i < this.mainScopeOps.getNumChunks(); i++) {
			workerEnvParams = new EnvParams();
			workerEnvParams.workerId = i;
			workerEnvParams.calculationPhase = CALC_PHASE_MAP;
			workerEnvParams.inputArgs = inputValues;
			workerEnvParams.modulesPaths = this.modulesPaths;
			worker = new MapWorker(workerEnvParams,
					new MultiThreadPhaseHandler(), this.bootstrapScript,
					this.datalibScript, this.userScript);
			Future<IntermediateResults> submit = executor.submit(worker);
			threadList.add(submit);
		}

		for (int i = 0; i < threadList.size(); i++) {
			try {
				mapResults.addAll(threadList.get(i).get());

			} catch (InterruptedException e) {
				mapResults.addError(e);
				LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);

			} catch (ExecutionException e) {
				mapResults.addError(e);
				LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);
			}
		}
		executor.shutdown();
		return mapResults;
	}

	/**
	 * Runs the REDUCE phase. Input is given by the result of thee MAP phase.
	 * 
	 * @return key => "object" for all emitted keys and values
	 */
	private IntermediateResults runReduce(IntermediateResults mapResults) {
		ExecutorService executor;
		List<Future<IntermediateResults>> threadList = new ArrayList<Future<IntermediateResults>>();
		IntermediateResults reduceResults = new IntermediateResults();
		int numReduceWorkers = this.mainScopeOps.getNumReduceWorkers();

		IntermediateResults[] mapResultPortions = groupResults(mapResults,
				numReduceWorkers);

		System.err.printf("Starting %d reduce workers: ", numReduceWorkers);

		executor = Executors.newFixedThreadPool(numReduceWorkers);
		for (int i = 0; i < numReduceWorkers; i++) {
			EnvParams workerEnvParams = new EnvParams();
			workerEnvParams.workerId = i;
			workerEnvParams.calculationPhase = CALC_PHASE_REDUCE;
			workerEnvParams.inputArgs = inputValues;
			workerEnvParams.modulesPaths = this.modulesPaths;

			ReduceWorker reduceWorker = new ReduceWorker(workerEnvParams,
					new MultiThreadPhaseHandler(), mapResultPortions[i],
					this.bootstrapScript, this.datalibScript, this.userScript);
			Future<IntermediateResults> submit = executor.submit(reduceWorker);
			threadList.add(submit);
		}
		System.err.printf("DONE\n");

		for (int i = 0; i < numReduceWorkers; i++) {
			try {
				reduceResults.addAll(threadList.get(i).get());

			} catch (InterruptedException e) {
				reduceResults.addError(e);
				LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);

			} catch (ExecutionException e) {
				reduceResults.addError(e);
				LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);
			}
		}
		executor.shutdown();
		return reduceResults;
	}

	/**
	 * 
	 * @param originalResults
	 * @param numGroups
	 * @return
	 */
	private IntermediateResults[] groupResults(
			IntermediateResults originalResults, int numGroups) {

		// TODO some smart assignment in case lists are very different in length
		// should be implemented
		IntermediateResults[] groups = new IntermediateResults[numGroups];
		List<Object> keys = new ArrayList<Object>(originalResults.keys());
		Random rand = new Random();

		for (int i = 0; i < groups.length; i++) {
			groups[i] = new IntermediateResults();
		}

		int j = 0;
		while (keys.size() > 0) {
			Object key = keys.remove(rand.nextInt(keys.size()));
			groups[j % numGroups].addMultiple(key, originalResults.remove(key));
			j++;
		}
		return groups;
	}

	/**
	 * 
	 */
	private String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

}
