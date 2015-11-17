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
import net.orzo.scripting.JsEngineAdapter;
import net.orzo.scripting.SourceCode;

/**
 * Handles a processing thread of a single data chunk. The result is returned as
 * {@link IntermediateResults} object.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class MapWorker implements Callable<IntermediateResults> {

    private final JsEngineAdapter jsEngine;

    private final SourceCode userScript;

    private final SourceCode[] sources;

    private final IntermediateResults intermediateData;

    /**
     *
     */
    public MapWorker(EnvParams envParams, SourceCode userScript,
            SourceCode... sourceCodes) {
        this.intermediateData = new IntermediateResults();
        this.jsEngine = new JsEngineAdapter(envParams, this.intermediateData);
        this.userScript = userScript;
        this.sources = sourceCodes;
    }

    /**
     *
     */
    @Override
    public IntermediateResults call() throws Exception {
        this.jsEngine.beginWork();
        this.jsEngine.runCode(this.sources);
        this.jsEngine.runFunction("initMap");
        this.jsEngine.runCode(this.userScript);
        this.jsEngine.runFunction("runMap");
        this.jsEngine.endWork();
        return this.intermediateData;
    }

}
