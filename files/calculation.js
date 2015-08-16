/*
 * Copyright (C) 2014 Tomas Machalek <tomas.machalek@gmail.com>
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

(function (scope) {
    'use strict';

    /*
     * Important notes:
     *
     * This code is loaded first (even before userenv.js).
     * It means that no Orzo's functions are available here.
     *
     * Both Worker and Main types must provide the same interface
     * to be able to respond to user's script in any phase (i.e.
     * user does not care about processing internals - she just
     * defines 'map', 'reduce', etc. functions)
     */


    /**
     * A multi-thread phase controller
     *
     * @constructor
     */
    function Worker() {
        this.applyItemsFn = null;
        this.dataChunksFn = null;
        this.mapFn = null;
        this.reduceFn = null;
    }

    /**
     *
     * @param numChunks unused in this type of worker
     * (because it is already operating in a single chunk
     * of some chunk set).
     * @param fn function to be registered as a chunk-processor
     */
    Worker.prototype.dataChunks = function (numChunks, fn) {
        this.dataChunksFn = fn;
    };

    /**
     *
     * @param fn
     */
    Worker.prototype.applyItems = function (fn) {
        this.applyItemsFn = fn;
    };

    /**
     * Registers 'map' function
     *
     * @param fn custom map function implementation
     */
    Worker.prototype.map = function (fn) {
        this.mapFn = fn;
    };

    /**
     * Registers 'reduce' function and optionally number of
     * workers for the reduce phase
     *
     * @param arg0 number of workers or reduce function
     * @param arg1 reduce function or undefined (in case reduce
     * is passed as arg0)
     */
    Worker.prototype.reduce = function (arg0, arg1) {
        if (arg1 === undefined) {
            this.reduceFn = arg0;

        } else {
            this.reduceFn = arg1;
        }
    };

    /**
     * Does nothing
     */
    Worker.prototype.finish = function () {
    };


    /**
     * A controller for single-thread phases
     *
     * @constructor
     */
    function Main() {
        this.numChunks = null;
        this.finishFn = null;
        this.numReduceWorkers = null;
    }

    /**
     * Registers the 'dataChunks' function
     *
     * @param numChunks number of chunks (= number of workers)
     * we want to process in parallel
     * @param fn not used in this controller
     */
    Main.prototype.dataChunks = function (numChunks, fn) {
        this.numChunks = numChunks;
    };

    /**
     * Does nothing here
     */
    Main.prototype.applyItems = function () {};

    /**
     * Does nothing here
     */
    Main.prototype.map = function () {};

    /**
     *
     * @param numWorkers
     * @param fn
     */
    Main.prototype.reduce = function (numWorkers, fn) {
        if (numWorkers > 0) {
            this.numReduceWorkers = numWorkers;

        } else {
            this.numReduceWorkers = this.numChunks;
        }
    };

    /**
     * Registers the 'finish' function
     *
     * @param fn
     */
    Main.prototype.finish = function (fn) {
        this.finishFn = fn;
    };

    /* ************************************************
     * Following functions initialize and start
     * individual processing phases (preparation, map,
     * reduce, finish)
     ************************************************** */

    /**
     * Returns an information about number
     * of threads to be used for map and reduce phases.
     * The function may be dynamically changed between different
     * calculation phases. Initially it just throws an exception.
     *
     * @return {{ numReduceWorkers : number, numChunks : number}}
     */
    scope.getParams = function () {
        throw new Error('Function getParams() not defined');
    };

    // reference to a current calculation controller
    scope._mr = null;

    /**
     * Runs the 'prepare' phase which means setting proper controller
     * (the '_mr' variable) for the subsequent phase and also setting
     * getParams() function based on user's script properties.
     */
    scope.prepare = function () {
        scope._mr = new Main();
        scope.getParams = function () {
            return {
                numReduceWorkers : scope._mr.numReduceWorkers,
                numChunks : scope._mr.numChunks
            }
        };
    };

    /**
     * Initializes JS environment for the 'map' phase
     */
    scope.initMap = function () {
        scope._mr = new Worker();
        // we set the default chunk resolver first
        // in case user does not define the applyItems() function
        scope._mr.applyItems(function (dataChunk, map) {
            while (dataChunk.hasNext()) {
                map(dataChunk.next());
            }
        });
    };

    /**
     * Runs the 'map' phase
     */
    scope.runMap = function () {
        var dataChunk = scope._mr.dataChunksFn(scope._env.workerId);
        scope._mr.applyItemsFn(dataChunk, scope._mr.mapFn);
    };

    /**
     * Initializes JS environment for the 'reduce' phase
     */
    scope.initReduce = function () {
        scope._mr = new Worker();
    };

    /**
     * Runs the 'reduce' phase
     *
     * @param key emitted key
     * @param data list of items belonging to the key
     */
    scope.runReduce = function (key, data) {
        scope._mr.reduceFn(key, data);
    };

    /**
     *
     * @param javaResults
     * @constructor
     */
    function FinalResults(javaResults) {
        var self = this;

        this._javaResults = javaResults;
        this.sorted = {
            each : function (fn) {
                self._each(fn, true);
            },
            get : function (key) {
                return self.et(key);
            },
            contains : function (key) {
                return self.contains(key);
            },
            keys : function () {
                return Java.from(self._javaResults.keys(true));
            }
        };
    }

    FinalResults.prototype._each = function (fn, sorted) {
        var keys = this._javaResults.keys(sorted),
            i;

        for (i = 0; i < keys.length; i += 1) {
            fn.call(this, keys[i], Java.from(this._javaResults.get(keys[i])));
        }
    };

    /**
     * @param fn
     */
    FinalResults.prototype.each = function (fn) {
        return this._each(fn, false);
    };

    FinalResults.prototype.get = function (key) {
        return Java.from(this._javaResults.get(key));
    };

    FinalResults.prototype.contains = function (key) {
        return this._javaResults.contains(key);
    };

    FinalResults.prototype.keys = function () {
        return Java.from(this._javaResults.keys(false));
    };

    /**
     * Initializes JS environment for the 'finish' phase
     */
    scope.initFinish = function () {
        scope._mr = new Main();
    };

    /**
     *
     * @param results
     * @param info
     */
    scope.runFinish = function (results, info) {
        return JSON.stringify(scope._mr.finishFn(new FinalResults(results), info));
    }

}(this));