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

(function (scope) {
    'use strict';

    /**
     * A helper function to allow two ways of passing array-like values to functions:
     * 1) a single argument of type Array
     * 2) multiple arguments (where each argument is considered to be
     * an array item)
     *
     * @param {Arguments} args
     * @returns {Array}
     */
    function fetchArgumentList(args) {
        if (args.length === 1 && Object.prototype.toString.call(args[0])) {
            return args[0];
        }
        return Array.prototype.slice.call(args, 0);
    }

    scope.orzo = {};
    scope.env = {};  // contains useful processing environment information
    scope.conf = {}; // modifies behavior of some functions

    // initialization of env values
    scope.env.workerId = scope._env.workerId;
    scope.env.inputArgs = []; // arguments passed via command-line

    (function () {
        var i;

        for (i = 0; i < scope._env.inputArgs.length; i += 1) {
            scope.env.inputArgs.push(String(scope._env.inputArgs[i]));
        }
    }());

    // global functions

    /**
     * @typedef {object} Iterator
     * @property {number} i
     * @property {*} data
     * @property {function} hasNext
     * @property {function} next
     */

    /**
     * Creates an iterator object from provided data. Please note that the iterator
     * always operates on the original data which means the data should not be
     * accessed in a concurrent way as long as the iterator exists and no more than
     * one iterator should be created for any data.
     *
     * @param {*} array
     * @param {function} [next]
     * @param {function} [hasNext]
     * @returns {Iterator}
     */
    scope.iterator = function (array, next, hasNext) {
        var nextFunc,
            hasNextFunc;

        if ({}.toString.call(array) !== '[object Array]'
                && (typeof hasNext !== 'function' || typeof next !== 'function')) {
            throw new TypeError('non Array object must be provided along with next() and hasNext() functions');
        }

        if (typeof next === 'function') {
            nextFunc = next;

        } else {
            nextFunc = function () {
                var ans = this.data[this.i];
                this.i += 1;
                return ans;
            };
        }

        if (typeof hasNext === 'function') {
            hasNextFunc = hasNext;

        } else {
            hasNextFunc = function () {
                return this.i < this.data.length;
            };
        }

        return {
            i: 0,
            data: array,
            hasNext: hasNextFunc,
            next: nextFunc
        };
    };

    /**
     * Registers a function which specifies how many chunks will be processed
     * simultaneously and how the individual chunks will be obtained.
     *
     * @param {number} numChunks number of chunks to be processed
     * @param {function(number)} callback function specifying how a chunk is obtained
     */
    scope.dataChunks = function (numChunks, callback) {
        scope.env.numChunks = numChunks;
        return scope._mr.dataChunks(numChunks, callback);
    };

    /**
     * Registers a function which specifies how a data chunk will be processed.
     *
     * @param  {function(number)} a function to be applied to each chunk
     */
    scope.applyItems = function (callback) {
        return scope._mr.applyItems(callback);
    };

    /**
     * Registers the "map" function
     *
     * @param {function(data)} a function to be applied to a single data item
     */
    scope.map = function (callback) {
        return scope._mr.map(callback);
    };

    /**
     * Registers the "reduce" function
     *
     * @param {function(string, array)} a function to be used to process all the data (parameters
     * represent single "key => [value1, value2,...]" as merged after all the map() functions are
     * executed)
     */
    scope.reduce = function (arg0, arg1) {
        var ans;

        if (typeof arg0 === 'function' && arg1 === undefined) {
            ans = scope._mr.reduce(0, arg0); // 0 forces orzo to use 'numChunks'

        } else if (typeof arg0 === 'number' && typeof arg1 === 'function') {
            ans = scope._mr.reduce(arg0, arg1);
        }
        return ans;
    };

    /**
     * Emits values from map and reduce functions.
     */
    scope.emit = function (arg0, arg1) {
        if (typeof arg0 !== 'string') {
            throw new Error('Cannot use non-string keys to emit values. Detected type: "'
                + (typeof arg0) + '".');
        }
        return scope._result.add(arg0, arg1);
    };

    /**
     * @typedef {{}} ReduceResults
     * @property {function(string, array)} each
     * @property {function} keys
     * @property {function(string)} get
     * @property {function(string}} contains
     */

    /**
     * @typedef {function} finishCallback
     * @param {ReduceResults} results
     */

    /**
     * This optional callback is called after the "reduce" is finished.
     * It can be used for example to export results, calculate some stats etc.
     *
     * @param {finishCallback} results
     */
    scope.finish = function (results) {
        return scope._mr.finish(results);
    };

    // ------------------------------------------------------------
    // global 'system' object methods wrapping custom Java methods
    // ------------------------------------------------------------

    /**
     * Prints passed argument's string representation
     * to the standard output. What complex objects show depends on
     * how their 'toString' method is implemented. If you want to
     * print internals of arrays and objects please refer to the 'dump'
     * method.
     *
     * @param  {object} obj an object to be print
     */
    scope.orzo.print = function (obj) {
        if (obj === null) {
            obj = 'null';
        }
        return scope._lib.strings.print(obj);
    };

    /**
     * Prints a string in the same way as Java PrintStream's printf
     *
     * @param {string} s
     * @param {...*} arguments parameters referenced in the formatting string
     */
    scope.orzo.printf = function (s) {
        var args = Array.prototype.slice.call(arguments, 1);
        return scope._lib.strings.printf(s, args);
    };

    /**
     * Formats a string by placing provided values into respective reference
     * placeholders. Works just like Java's String.format().
     *
     * @param {string} s
     * @param {...*} arguments parameters referenced in the formatting string
     * @return {string} resulting string
     */
    scope.orzo.sprintf = function (s) {
        var args = Array.prototype.slice.call(arguments, 1);
        return String(scope._lib.strings.sprintf(s, args));
    };

    /**
     * Dumps an object to a string. This is intended for debugging purposes.
     * Function is recursive in the sense that it dumps composed object down
     * to the last pieces.
     *
     * @param {object} obj any object
     * @return {string}
     */
    scope.orzo.dump = function (obj) {
        function dumpObj(o) {
            var buff = [],
                ans = '',
                prop,
                objType = {}.toString.call(o);

            if (objType === '[object Array]' || objType === '[object JavaArray]') {
                o.forEach(function (v) {
                    buff.push(dumpObj(v));
                });
                ans = '[' + buff.join(', ') + ']';

            } else if (objType === '[object Object]') {
                for (prop in o) {
                    if (o.hasOwnProperty(prop)) {
                        buff.push(prop + ' : ' + dumpObj(o[prop]));
                    };
                }
                ans = '{' + buff.join(', ') + '}';

            } else if (o !== null && o !== undefined) {
                ans = o.toString();

            } else {
                ans = o;
            }
            return ans;
        }
        scope._lib.strings.print(dumpObj(obj));
    };

    /**
     * @typedef {object} BaseIterator
     * @property {function} hasNext tests whether there is a next object
     * @property {function} next returns next object or undefined if there are no objects
     */

    /**
     * Generic function which produces multiple directory reader types. It is not
     * intended to be used externally.
     *
     * @param pathInfo
     * @param chunkId
     * @param filter
     * @param {string} javaFnName
     * @param {function} itemExtract
     * @returns {BaseIterator}
     */
    function generalDirectoryReader(pathInfo, chunkId, filter, javaFnName, itemExtract) {
        var iterator = {},
            reader,
            pathList;

        if (typeof pathInfo === 'string') {
            pathList = [pathInfo];

        } else {
            pathList = pathInfo;
        }

        if ({}.toString.call(filter) === '[object RegExp]') {
            filter = filter.toString();

        } else if (filter && typeof filter !== 'string') {
            throw new Error('The filter must be either a string or a RegExp. Type found: ' + (typeof filter));
        }

        reader = scope._lib.files[javaFnName](pathList, scope.env.numChunks, filter || null);
        iterator._javaIterator = reader.getIterator(chunkId);

        iterator.hasNext = function () {
            return iterator._javaIterator.hasNext();
        };

        iterator.next = function () {
            var files;

            try {
                files = iterator._javaIterator.next();

            } catch(e) {
                if (e.javaException instanceof java.util.NoSuchElementException) {
                    files = undefined;

                } else {
                    throw e;
                }
            }
            return itemExtract(files);
        };

        return iterator;
    }

    /**
     * Returns iterators covering split list of files.
     * File = [<iterator 0>,<iterator 1>,...,<iterator N>]
     *
     * @param {string|array} pathInfo a directory path or a list of directory paths to be searched
     * @param {number} chunkId
     * @param {string|RegExp} [filter] an optional regular expression specifying names to be accepted
     * @returns {BaseIterator}
     */
    scope.orzo.directoryReader = function (pathInfo, chunkId, filter) {
        return generalDirectoryReader(
            pathInfo,
            chunkId,
            filter,
            'directoryReader',
            function (x) {
                return String(x);
            }
        );
    };

    /**
     * Reads all the files in a directory (just like directoryReader) and generates all the
     * file pairs. Order is ignored which means pairs [A, B] and [B, A] are equivalent and only
     * one of them will be part of the result. Number of generated pairs is N * (N - 1) / 2.
     *
     * @param pathInfo
     * @param chunkId
     * @param filter
     * @returns {{}}
     */
    scope.orzo.filePairGenerator = function (pathInfo, chunkId, filter) {
        return generalDirectoryReader(
            pathInfo,
            chunkId,
            filter,
            'filePairGenerator',
            function (x) {
                return [String(x[0]), String(x[1])];
            }
        );
    };


    /**
     * Obtains an iterator which reads provided file (specified by path) line by
     * line. Iterator can be accessed by a classic method pair hasNext()
     * and next().
     *
     * @param  {string} path path to a file
     * @return {BaseIterator}
     */
    scope.orzo.fileReader = function (path) {
        var reader = {};

        reader._javaReader = scope._lib.files.fileReader(path);

        reader.hasNext = function () {
            return reader._javaReader.hasNext();
        };

        reader.next = function () {
            return String(reader._javaReader.next());
        };

        return reader;
    };

    /**
     * Creates or returns existing file chunk reader identified by the file path and chunkId.
     *
     * @param {string} path path to the file we want to read
     * @param {number} chunkId index of required chunk (starts from zero)
     * @param {number} [chunkSize=null] chunk size in lines; if omitted then automatic estimation is performed
     * @param {number} [startLine=0] first line to read (0 by default)
     * @return {BaseIterator}
     */
    scope.orzo.fileChunkReader = function (path, chunkId, chunkSize, startLine) {
        var iterator = {},
            fcrFactory;

        fcrFactory = scope._lib.files.filePartReaderFactory(path, scope.env.numChunks,
            chunkSize ? chunkSize : null, startLine ? startLine : 0);

        iterator._javaIterator = fcrFactory.createInstance(chunkId);

        iterator.hasNext = function () {
            return iterator._javaIterator.hasNext();
        };

        iterator.next = function () {
            var value;

            try {
                value = String(iterator._javaIterator.next());

            } catch (e) {
                if (e.javaException instanceof java.util.NoSuchElementException) {
                    value = undefined;

                } else {
                    throw e;
                }
            }
            return value;
        };

        return iterator;
    };

    /**
     * Saves a string to a file in a synchronous way
     *
     * @param {string} path
     * @param {string} text a text to be saved
     * @return {bool} true on success else false
     */
    scope.orzo.saveText = function (path, text) {
        return scope._lib.files.saveText(path, text);
    };

    /**
     * Reads whole file into a string. It is intended to read
     * smaller files (e.g. script configuration).
     *
     * @param {string} path a path to a file
     * @return {string} file contents or null in case of an error
     */
    scope.orzo.readText = function (path) {
        return scope._lib.files.readText(path);
    };

    /**
     * Imports JSON encoded data from a file.
     *
     * @param {string} path a path to a file
     * @return {*} an object as read and decoded from respective JSON data
     */
    scope.orzo.readJSON = function (path) {
        return JSON.parse(scope.orzo.readText(path));
    };

    /**
     * @typedef {object} FileWriter
     * @property {string} path
     * @property {function} writeln
     * @property {function} write
     * @property {function} close
     */

    /**
     * A file writer based on Java's BufferedWriter.
     *
     * @param path
     * @returns {FileWriter}
     */
    scope.orzo.fileWriter = function (path) {
        var ans = {};

        ans._javaWriter = scope._lib.files.createTextFileWriter(path);

        ans.path = path;

        ans.writeln = function (s) {
            var value = String(s);

            ans._javaWriter.write(value, 0, value.length);
            ans._javaWriter.newLine();
        };

        ans.write = function (s) {
            var value = String(s);

            ans._javaWriter.write(String(value), 0, value.length);
        };

        ans.close = function () {
            ans._javaWriter.close();
        };

        return ans;
    };

    /**
     * Calculates a md5 hash of an object
     *
     * @param  {object} s object with some string representation
     * @return {string} md5 hash of provided object (toString() is used to convert it)
     */
    scope.orzo.md5 = function (s) {
        return String(scope._lib.strings.md5(s.toString()));
    };

    /**
     * Returns a number of available processors. Please note that Intel CPUs with
     * hyper-threading report twice as high as is actual number of physical
     * cores.
     *
     * @return {number} number of cores/virtual cores/processors
     */
    scope.orzo.numOfProcessors = function () {
        return scope._lib.numOfProcessors();
    };

    /**
     * Creates a native JavaScript array. It should be faster than doing this in
     * JavaScript.
     *
     * @param {number} size
     * @return {array} native JavaScript array
     */
    scope.orzo.array = function (size) {
        return scope._lib.dataStructures.array(size);
    };

    /**
     * Creates a native JavaScript zero-filled array. It should be faster than
     * doing this in JavaScript.
     *
     * @param size
     * @return {Array} JavaScript array
     */
    scope.orzo.zeroFillArray = function (size) {
        return scope._lib.dataStructures.zeroFillArray(size);
    };

    /**
     * Creates a numeric matrix of a specified size. Returned value
     * is a normal JavaScript array (of arrays).
     *
     * @param width
     * @param height
     * @returns {array<array>}
     */
    scope.orzo.numericMatrix = function (width, height) {
        return scope._lib.dataStructures.numericMatrix(width, height);
    };

    /**
     * Creates an array of numbers starting from arg0 with increment 1
     * up to arg1 - 1. If only a single argument is passed then values
     * from 0 to arg0 - 1 are generated.
     *
     * @param {number} arg0
     * @param {number} [arg1]
     * @returns {Array}
     */
    scope.orzo.range = function (arg0, arg1) {
        var from, to, data, i;

        if (typeof arg1 === 'undefined') {
            from = 0;
            to = arg0;

        } else {
            from = arg0;
            to = arg1;
        }

        data = scope.orzo.zeroFillArray(to - from);
        for (i = 0; i < data.length; i += 1) {
            data[i] = i + from;
        }
        return data;
    };

    /**
     * Creates an array of unique items out of an existing array. In case values
     * to be compared are wrapped in a structured data type, an optional access
     * function can be passed. Internally, a Java HashSet type is used here.
     *
     * @param {array} data
     * @param {function} [keyFn]
     * @returns {array}
     */
    scope.orzo.uniq = function (data, keyFn) {
        return scope._lib.dataStructures.uniq(data, keyFn || null);
    };

    /**
     * Converts 2D numeric matrix into a 1D vector; line-by-line
     *
     * @param {array} 2D matrix
     * @returns {array} output 1D vector
     */
    scope.orzo.flattenMatrix = function (matrix) {
        return scope._lib.dataStructures.flattenMatrix(matrix);
    };

    /**
     * Measures the execution time of the provided function. Please note that in
     * case of asynchronous code you may not obtain the value you have been
     * expecting.
     *
     * @param {function} fn a function to be measured
     * @return time in milliseconds
     */
    scope.orzo.measureTime = function (fn) {
        return scope._lib.measureTime(fn);
    };

    /**
     * @typedef {object} Report
     * @property {function} title get or set title
     * @property {function} cols get or set column headers
     * @property {function} addRow adds a new data row
     * @property {function} close generates HTML report and closes all resources
     */

    /**
     *
     * @param {string} outputPath
     * @returns {Report}
     */
    scope.orzo.report = function (outputPath) {
        var fileWriter = scope.orzo.fileWriter(outputPath);

        return {
            data : {
                title : 'Orzo.js - a result report',
                cols : [],
                rows : []
            },

            title : function (title) {
                if (typeof title !== 'undefined') {
                    this.data.title = title;

                } else {
                    return this.data.title;
                }

            },
            cols : function () {
                if (arguments.length > 0) {
                    this.data.cols = fetchArgumentList(arguments);

                } else {
                    return this.data.cols;
                }
            },
            addRow : function () {
                this.data.rows.push(fetchArgumentList(arguments));
            },
            close : function () {
                try {
                    fileWriter.write(
                        scope._lib.templating.renderTemplate("net/orzo/report1.jade",
                            "net/orzo/orzo.css",
                            this.data)
                    );

                } finally {
                    fileWriter.close();
                }
            }
        };
    };

    /**
     *
     * @typedef {object} Image
     * @property {number} width
     * @property {number} height
     * @property {string} path
     * @property {function} toArray
     * @property {function} areaToVector
     * @property {function} areaHistogram
     */

    /**
     *
     * @param javaImage
     * @param path
     * @returns {Image}
     */
    function instantiateImage(javaImage, path) {

        /**
         *
         * @param width
         * @param height
         * @param path
         * @constructor
         */
        function Image(width, height, path) {
            this.width = width;
            this.height = height;
            this.path = path;
        }

        Image.prototype = {
            __javaImage : javaImage
        };

        Image.prototype.toArray = function () {
            return this.__javaImage.toArray();
        };

        Image.prototype.areaToVector = function (x, y, width, height) {
            if (arguments.length === 0) {
                x = 0;
                y = 0;
                width = this.width;
                height = this.height;
            }
            return this.__javaImage.areaToVector(x, y, width, height);
        };

        Image.prototype.areaHistogram = function (x, y, width, height) {
            if (arguments.length === 0) {
                return this.__javaImage.histogram();

            } else {
                return this.__javaImage.areaHistogram(x, y, width, height);
            }
        };

        return new Image(javaImage.getWidth(), javaImage.getHeight(), path);
    };


    /**
     * Load
     *
     * @param path
     * @returns {Image}
     */
    scope.orzo.loadImage = function (path) {
        var javaImg = scope._lib.loadImage(path);

        if (javaImg) {
            return instantiateImage(javaImg, path);
        }
        return null;
    };

    /**
     * Provides python-like 'with' guarded block
     *
     * @param {{}} obj an object with 'close()' method
     * @param {function} fn an action to be done with 'obj' as an argument
     * @param {function} [err]
     */
    scope.doWith = function (obj, fn, err) {
        try {
            fn.call(obj, obj);

        } catch (e) {
            if (typeof err === 'function') {
                err(e);
            }

        } finally {
            if (obj.hasOwnProperty('close') && typeof obj.close === 'function') {
                obj.close();
            }
        }
    };


}(this));

/*jslint nomen: true */