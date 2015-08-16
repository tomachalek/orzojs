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

import static net.orzo.Util.normalizePath;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.orzo.scripting.EnvParams;
import net.orzo.scripting.JsEngineAdapter;
import net.orzo.service.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs map-reduce calculation.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
@SuppressWarnings("restriction")
public class Calculation {

	/**
	 * 
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(Calculation.class);

	private final CalculationParams params;

	/**
	 * 
	 */
	private final List<String> modulesPaths;

	/**
	 * 
	 */
	private final String[] inputValues;


	private final Consumer<TaskStatus> statusListener;

	/**
	 * 
	 * @param mainThreadOps
	 * @param userenvScript
	 * @param userScript
	 */
	public Calculation(CalculationParams params,
			Consumer<TaskStatus> statusListener) {
		this.params = params;
		this.statusListener = statusListener;
		this.inputValues = params.inputValues;
		this.modulesPaths = new ArrayList<String>();
		this.modulesPaths.add(params.workingDirModulesPath);
		if (params.optionalModulesPath != null) {
			this.modulesPaths.add(params.optionalModulesPath);
		}
	}

	/**
	 * Starts and controls the calculation.
	 * 
	 */
	public String run() throws CalculationException {
		String ans = null;
		IntermediateResults mapResults;
		IntermediateResults reduceResults = null;
		int numChunks;
		int numReduceWorkers;
		long startTime = System.currentTimeMillis();
		CalculationInfo calcInfo = new CalculationInfo();
		calcInfo.put("datetime", getCurrentDate());

		try {
			ScriptObjectMirror prepareData = runPrepare();
			numReduceWorkers = (int) prepareData.get("numReduceWorkers");
			numChunks = (int) prepareData.get("numChunks");

			mapResults = runMap(numChunks);

			if (!mapResults.hasErrors()) {
				reduceResults = runReduce(numReduceWorkers, mapResults);
			}
			calcInfo.put("duration",
					(System.currentTimeMillis() - startTime) / 1000.0);
			System.err.printf("\n\nFinished in %01.3f seconds.\n",
					calcInfo.get("duration"));

			if (reduceResults != null && !reduceResults.hasErrors()) {
				ans = runFinish(reduceResults, calcInfo);
				this.statusListener.accept(TaskStatus.FINISHED);

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
				this.statusListener.accept(TaskStatus.ERROR);
			}
			return ans;

		} catch (ScriptException | NoSuchMethodException e) {
			throw new CalculationException("Calculation failed: "
					+ e.getMessage(), e);
		}
	}

	private EnvParams createEnvParams() {
		EnvParams envParams = new EnvParams();
		envParams.workingDir = normalizePath(System.getProperty("user.dir"));
		envParams.scriptName = normalizePath(this.params.userScript
				.getFullyQualifiedName());
		envParams.inputArgs = this.inputValues;
		envParams.modulesPaths = this.modulesPaths;
		return envParams;
	}

	/**
	 * Runs preparation phase when user script is loaded (= all the respective
	 * functions are registered but no real processing is done yet).
	 * 
	 * @return
	 */
	private ScriptObjectMirror runPrepare() throws ScriptException,
			NoSuchMethodException {
		JsEngineAdapter jsEngine = new JsEngineAdapter(createEnvParams());
		jsEngine.beginWork();
		jsEngine.runCode(this.params.calculationScript,
				this.params.userenvScript, this.params.datalibScript);
		jsEngine.runFunction("prepare");
		jsEngine.runCode(this.params.userScript);
		return (ScriptObjectMirror) jsEngine.runFunction("getParams");
	}

	/**
	 * Runs the MAP phase. Input is given by user's script,output is
	 * {@link IntermediateResults} which stores respective keys and lists of
	 * values.
	 * 
	 * @return key => [value1, value2,..., valueN] for all emitted keys and
	 *         values
	 * @throws ScriptException
	 */
	private IntermediateResults runMap(int numWorkers) throws ScriptException {
		ExecutorService executor;
		List<Future<IntermediateResults>> threadList = new ArrayList<Future<IntermediateResults>>();
		Callable<IntermediateResults> worker;
		IntermediateResults mapResults = new IntermediateResults();

		executor = Executors.newFixedThreadPool(numWorkers);
		EnvParams workerEnvParams;
		for (int i = 0; i < numWorkers; i++) {
			workerEnvParams = createEnvParams();
			workerEnvParams.workerId = i;
			worker = new MapWorker(workerEnvParams, this.params.userScript,
					this.params.calculationScript, this.params.userenvScript,
					this.params.datalibScript);
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
	private IntermediateResults runReduce(int numWorkers,
			IntermediateResults mapResults) {
		ExecutorService executor;
		List<Future<IntermediateResults>> threadList = new ArrayList<Future<IntermediateResults>>();
		IntermediateResults reduceResults = new IntermediateResults();

		IntermediateResults[] mapResultPortions = groupResults(mapResults,
				numWorkers);

		System.err.printf("Starting %d reduce workers: ", numWorkers);

		executor = Executors.newFixedThreadPool(numWorkers);
		for (int i = 0; i < numWorkers; i++) {
			EnvParams workerEnvParams = createEnvParams();
			workerEnvParams.workerId = i;

			ReduceWorker reduceWorker = new ReduceWorker(workerEnvParams,
					mapResultPortions[i], this.params.userScript,
					this.params.calculationScript, this.params.userenvScript,
					this.params.datalibScript);
			Future<IntermediateResults> submit = executor.submit(reduceWorker);
			threadList.add(submit);
		}
		System.err.printf("DONE\n");

		for (int i = 0; i < numWorkers; i++) {
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

	private String runFinish(IntermediateResults reduceResults,
			CalculationInfo calcInfo) throws ScriptException,
			NoSuchMethodException {
		String ans;
		EnvParams envParams = createEnvParams();
		JsEngineAdapter jse = new JsEngineAdapter(envParams);
		FinalResults fr = new FinalResults(reduceResults);

		jse.beginWork();
		jse.runCode(this.params.calculationScript, this.params.userenvScript,
				this.params.datalibScript);
		jse.runFunction("initFinish");
		jse.runCode(this.params.userScript);
		ans = (String) jse.runFunction("runFinish", fr, calcInfo);
		jse.endWork();
		return ans;
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
