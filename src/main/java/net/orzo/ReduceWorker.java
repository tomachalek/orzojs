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
import net.orzo.scripting.SourceCode;

/**
 * Handles a processing thread of the REDUCE phase.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ReduceWorker implements Callable<IntermediateResults> {

    private final SourceCode userScript;

    private final SourceCode[] sources;

    private final IntermediateResults mapResults;

    private final List<Object> keys;

    private final JsEngineAdapter jsEngine;

    /**
     */
    public ReduceWorker(JsEngineAdapter jsEngine, IntermediateResults mapResults,
                        List<Object> keys, SourceCode userScript,
                        SourceCode... sourceCodes) {
        this.mapResults = mapResults;
        this.keys = keys;
        this.userScript = userScript;
        this.jsEngine = jsEngine;
        this.sources = sourceCodes;
    }

    @Override
    public IntermediateResults call() throws Exception {
        this.jsEngine.beginWork();
        this.jsEngine.runCode(this.sources);
        this.jsEngine.runFunction("initReduce");
        this.jsEngine.runCode(this.userScript);
        for (Object key : this.keys) {
            this.jsEngine.runFunction("runReduce", key, this.mapResults.values(key));
            this.mapResults.remove(key);
        }
        return this.jsEngine.getIntermediateResults();
    }

}