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

import java.util.ArrayList;
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
import net.orzo.service.TaskEvent;
import net.orzo.service.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs map-reduce calculation.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
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

    /**
     *
     */
    public Calculation(CalculationParams params) {
        this.params = params;
        this.inputValues = params.inputValues;
        this.modulesPaths = new ArrayList<>();
        this.modulesPaths.add(params.workingDirModulesPath);
        if (params.optionalModulesPath != null) {
            this.modulesPaths.add(params.optionalModulesPath);
        }
    }

    /**
     * Starts and controls the calculation.
     */
    public Object run() throws CalculationException {
        IntermediateResults mapResults;
        IntermediateResults reduceResults;
        ScriptObjectMirror prepareData = runPrepare();
        mapResults = runMap(prepareData);
        reduceResults = runReduce(prepareData, mapResults);
        return runFinish(reduceResults);
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
     */
    private ScriptObjectMirror runPrepare() throws CalculationException {
        LOG.info("Running PREPARE phase.");
        JsEngineAdapter jsEngine = new JsEngineAdapter(createEnvParams());
        jsEngine.beginWork();
        try {
            jsEngine.runCode(this.params.calculationScript,
                    this.params.userenvScript, this.params.datalibScript);
            jsEngine.runFunction("prepare");
            jsEngine.runCode(this.params.userScript);
            return (ScriptObjectMirror) jsEngine.runFunction("getParams");

        } catch (NoSuchMethodException | ScriptException ex) {
            throw new CalculationException("Failed to perform PREPARE: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Runs the MAP phase. Input is given by user's script,output is
     * {@link IntermediateResults} which stores respective keys and lists of
     * values.
     *
     * @return key => [value1, value2,..., valueN] for all emitted keys and
     * values
     */
    private IntermediateResults runMap(ScriptObjectMirror conf)
            throws CalculationException {
        ExecutorService executor;
        List<Future<IntermediateResults>> threadList = new ArrayList<>();
        Callable<IntermediateResults> worker;
        IntermediateResults mapResults = new IntermediateResults();
        int numWorkers = (int) conf.get("numChunks");

        LOG.info(String.format("Starting %d MAP workers.", numWorkers));

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

        List<Exception> errors = new ArrayList<>();
        for (int i = 0; i < threadList.size(); i++) {
            try {
                mapResults.addAll(threadList.get(i).get());

            } catch (InterruptedException | ExecutionException e) {
                errors.add(e);
                LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);
            }
        }
        executor.shutdown();
        if (errors.size() > 0) {
            throw new ParallelException("Failed to perform MAP", errors);
        }
        return mapResults;
    }

    /**
     * Runs the REDUCE phase. Input is given by the result of thee MAP phase.
     *
     * @return key => "object" for all emitted keys and values
     */
    private IntermediateResults runReduce(ScriptObjectMirror prepareData,
                                          IntermediateResults mapResults) throws ParallelException {
        ExecutorService executor;
        List<Future<IntermediateResults>> threadList = new ArrayList<>();
        IntermediateResults reduceResults = new IntermediateResults();
        int numWorkers = (int) prepareData.get("numReduceWorkers");

        IntermediateResults[] mapResultPortions = groupResults(mapResults,
                numWorkers);

        LOG.info(String.format("Starting %d REDUCE workers.", numWorkers));

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

        List<Exception> errors = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            try {
                reduceResults.addAll(threadList.get(i).get());

            } catch (InterruptedException | ExecutionException e) {
                errors.add(e);
                LOG.error(String.format("Worker[%d]: %s", i, e.getMessage()), e);
            }
        }
        executor.shutdown();
        if (errors.size() > 0) {
            throw new ParallelException("Failed to perform REDUCE.", errors);
        }
        return reduceResults;
    }

    private Object runFinish(IntermediateResults reduceResults)
            throws CalculationException {
        Object ans;
        EnvParams envParams = createEnvParams();
        JsEngineAdapter jse = new JsEngineAdapter(envParams);
        FinalResults fr = new FinalResults(reduceResults);

        LOG.info("Running FINISH phase");

        jse.beginWork();
        try {
            jse.runCode(this.params.calculationScript,
                    this.params.userenvScript, this.params.datalibScript);
            jse.runFunction("initFinish");
            jse.runCode(this.params.userScript);
            ans = jse.runFunction("runFinish", fr);

        } catch (NoSuchMethodException | ScriptException ex) {
            throw new CalculationException("Failed to perform FINISH: "
                    + ex.getMessage(), ex);
        }
        jse.endWork();
        return ans;
    }

    /**
     */
    private IntermediateResults[] groupResults(
            IntermediateResults originalResults, int numGroups) {

        // TODO some smart assignment in case lists are very different in length should be implemented
        IntermediateResults[] groups = new IntermediateResults[numGroups];
        List<Object> keys = new ArrayList<>(originalResults.keys());
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

}
