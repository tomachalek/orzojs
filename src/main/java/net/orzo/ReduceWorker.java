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

import java.util.List;
import java.util.concurrent.Callable;

import net.orzo.scripting.JsEngineAdapter;

/**
 * Handles a processing thread of the REDUCE phase.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ReduceWorker implements Callable<IntermediateResults> {

    private final CalculationParams params;

    private final IntermediateResults mapResults;

    private final List<Object> keys;

    private final int functionIdx;

    private final JsEngineAdapter jsEngine;

    /**
     * @param jsEngine
     *      a JS engine the worker will be using to process its task
     * @param mapResults
     *      an object holding all the merged results from map phase
     * @param keys
     *      a subset of keys from mapResults which will be processed by this worker
     * @param functionIdx
     *      which reduce function will be used (user may define one or more reduce functions
     *      to be able to perform re-reduce)
     * @param params
     */
    public ReduceWorker(JsEngineAdapter jsEngine, IntermediateResults mapResults,
                        List<Object> keys, int functionIdx, CalculationParams params) {
        this.mapResults = mapResults;
        this.keys = keys;
        this.functionIdx = functionIdx;
        this.jsEngine = jsEngine;
        this.params = params;
    }

    @Override
    public IntermediateResults call() throws Exception {
        this.jsEngine.beginWork();
        this.jsEngine.runCode(this.params.calculationScript, this.params.userenvScript,
                this.params.datalibScript);
        this.jsEngine.runFunction("initReduce");
        this.jsEngine.runCode(this.params.userScript);
        for (Object key : this.keys) {
            this.jsEngine.runFunction("runReduce", key, this.mapResults.values(key), this.functionIdx);
            this.mapResults.remove(key);
        }
        return this.jsEngine.getIntermediateResults();
    }

}