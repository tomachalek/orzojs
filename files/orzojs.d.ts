/*
 * Copyright (C) 2015 Tomas Machalek <tomas.machalek@gmail.com>
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

/**
 * Specifies MAP's signature
 */
interface MapFunction<T> {
    (callback:(v:T)=>void):void
}

/**
 *
 */
interface Results {
    each(fn:(key:string, values:Array<any>)=>void):void;
}

/**
 *
 */
interface Closeable {
    close():void;
}

/**
 * A general iterator used by Orzojs to access different types
 * of data.
 */
interface Iterator<T> {

    /**
     * Returns the next element. If there is no such element
     * available then 'undefined' should be returned.
     */
    next():T;

    /**
     * Tests whether the iterator contains one more element
     */
    hasNext():boolean;
}

/**
 * A library containing function to work with array-like
 * data with heterogeneous items.
 */
declare module datalib {

    /**
     * General array-like data wrapper. Individual elements can
     * be of any type but there must be a single function available
     * which produces a 'number' type element from the original one.
     *
     * The Data object has to know how to access a numeric value inside
     * original item of type T. If no such access function is provided
     * then simple identity (f(x) = x) is used.
     */
    export interface Data<T> {

        /**
         * Returns number of elements
         */
        size():number;

        /**
         * Returns numeric value extracted from individual
         * element with index i
         *
         * @param i
         */
        get(i:number):number;

        /**
         * Iterates over original data items and applies passed function
         * on them.
         * To break the iteration function must return false.
         *
         * @param fn a function to be applied on each value
         */
        each(fn:(v:T, i:number)=>void):void;

        /**
         * Calculates the sum of provided numbers.
         * If a non-number is encountered then NaN is returned.
         */
        sum():number;

        /**
         * Finds maximal element in the data. If there is
         * even a single non-numerical element then NaN is returned.
         */
        max():number;

        /**
         * Finds maximal element in the data. If there is
         * even a single non-numerical element then NaN is returned.
         */
        min():number;

        /**
         * Calculates arithmetic average of provided numbers
         */
        average():number;

        /**
         * Calculates standard deviation of the sample. NaN is returned
         * in case the value cannot be calculated from any reason (non-numeric
         * value encountered, zero divison etc.).
         */
        stdev():number;

        /**
         * Calculates Pearson product-moment correlation coefficient
         * between this data and other data.
         * (http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient)
         *
         * @param other
         */
        correl(other:Data):number;
    }


    /**
     * Returns values of an object (i.e. the values of all object's own properties).
     * Optionally, a transform function can be provided to change specific values into
     * other ones (e.g. undefined to zero).
     *
     * @param obj
     * @param transform optional function to transform values
     * @return list of values
     * @throws {Error} If the obj argument is null or of a non-object type
     */
    export function values(obj:any, transform?:(v:any)=>any):Array<any>;
}


/**
 *
 */
interface DOMQueryResults {
    [query:string]:Array<string>;
}


/**
 * General text file writer. Please note that the object
 * should be closed to make sure all the changes are really
 * stored. You can use doWith() function which handles this
 * automatically.
 */
interface FileWriter {

    /**
     * Path of the file writer writes to.
     * This should be treated as read-only.
     */
    path:string;

    /**
     * Writes a single line to the file. Used new-line character
     * is platform-dependent.
     *
     * @param s
     */
    writeln(s:string);

    /**
     * Writes a string to the file without trailing end-line character.
     *
     * @param s
     */
    write(s:string);

    /**
     * Closes the writer. This must be called to ensure all the data
     * is actually written to the file.
     */
    close();
}

/**
 * Represents a greyscale bitmap image.
 */
interface Image {

    /**
     *
     */
    width:number;

    /**
     *
     */
    height:number;

    /**
     * Exports the image line by line as a 1-D vector.
     */
    toArray():Array<number>;

    /**
     * Exports a defined area (starting from upper-left corner with defined position
     * and size) into a 1-D vector.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    areaToVector(x:number, y:number, width:number, height:number):Array<number>;

    /**
     * Creates a histogram of images pixels' intensity values (0...255). The
     * resulting vector (of size 256) contains ratio of pixels of the values
     * in image (i.e each value is between 0 and 1).
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    areaHistogram(x:number, y:number, width:number, height:number):Array<number>;
}


/**
 * This interface represents an HTML document.
 * It is just a subset of JSoup's Document interface.
 */
interface Document {
    html():string;
    body():Element;
    head():Element;
}


/**
 * This interface represents a list of Element instances.
 * It is just a subset of JSoup's Elements interface.
 */
interface Elements {
    select(query:string):Elements;
}

/**
 * This interface represents an element in an HTML document.
 * It is just a subset of JSoup's Element interface.
 */
interface Element {

    nodeName():string;

    tagName():string;

    /**
     * Sets a new name for the tag
     *
     * @param name
     */
    tagName(name:string):Element;

    parent():Element;

    /**
     * All the ancestors up to the root
     */
    parents():Elements;

    previousElementSibling():Element;

    nextElementSibling():Element;

    siblingElements():Elements;

    attr(name:string):string;

    children():Elements;

    /**
     * Returns whole subtree starting from this element (and including this element)
     */
    getAllElements():Elements;

    id():string;

    nodeName():string;

    text():string;

    /**
     * data- attributes
     */
    dataset():{[key:string]:string};
}

/**
 *
 */
interface env {

    /**
     * Contains command line parameters of user's scripts. Orzojs' own
     * parameters are excluded.
     *
     * E.g. calling:
     *   orzojs -m /my/libs myscript.js /my/data/dir /my/output/dir
     *
     * produces following inputArgs:
     *   ['/my/data/dir', '/my/output/dir']
     */
    inputArgs:Array<any>;
}


/**
 * Orzojs core library
 */
declare module orzo {

    /**
     * Prints passed argument's string representation
     * to the standard output. What complex objects show depends on
     * how their 'toString' method is implemented. If you want to
     * print internals of arrays and objects please refer to the 'dump'
     * method.
     *
     * @param v
     */
    function print(v:any):void;

    /**
     * Prints a string in the same way as Java PrintStream's printf
     *
     * @param s A string to be printed
     * @param values Values to replace formatting placeholders in the string
     */
    function printf(s:string, ...values:Array<any>);

    /**
     * Formats a string by placing provided values into respective reference
     * placeholders. Works just like Java's String.format().
     *
     * @param s A string to be printed
     * @param values Values to replace formatting placeholders in the string
     */
    function sprintf(v:string, ...values:Array<any>):string;

    /**
     * Prints internals of passed object. This is intended for debugging purposes.
     *
     * @param obj
     */
    function dump(obj:any):void;

    /**
     * Returns iterators covering split list of files.
     * File = [<iterator 0>,<iterator 1>,...,<iterator N>]
     *
     * @param pathInfo A directory path or a list of directory paths to be searched
     * @param chunkId
     * @param filter An optional regular expression specifying names to be accepted
     */
    // TODO union types (TS 1.4 supports them)
    function directoryReader(pathInfo:string, chunkId:number, filter?:RegExp):Iterator;
    function directoryReader(pathInfo:string, chunkId:number, filter?:string):Iterator;
    function directoryReader(pathInfo:Array<string>, chunkId:number, filter?:RegExp):Iterator;
    function directoryReader(pathInfo:Array<string>, chunkId:number, filter?:string):Iterator;

    /**
     *
     * Reads all the files in a directory (just like directoryReader) and generates all the
     * file pairs. Order is ignored which means pairs [A, B] and [B, A] are equivalent and only
     * one of them will be part of the result. Number of generated pairs is N * (N - 1) / 2.
     *
     * @param pathInfo
     * @param chunkId
     * @param filter
     */
    // TODO union types (TS 1.4 supports them)
    function filePairGenerator(pathInfo:string, chunkId:number, filter?:RegExp):Iterator;
    function filePairGenerator(pathInfo:string, chunkId:number, filter?:string):Iterator;
    function filePairGenerator(pathInfo:Array<string>, chunkId:number, filter?:RegExp):Iterator;
    function filePairGenerator(pathInfo:Array<string>, chunkId:number, filter?:string):Iterator;

    /**
     * Generates a Cartesian product of two groups of files.
     *
     * @param pathInfo1
     * @param pathInfo2
     * @param chunkId
     * @param filter
     */
    // TODO union types (TS 1.4 supports them)
    function twoGroupFilePairGenerator(pathInfo1:string, pathInfo2:string, chunkId:number, filter?:RegExp):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:string, pathInfo2:string, chunkId:number, filter?:string):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:Array<string>, pathInfo2:string, chunkId:number, filter?:RegExp):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:Array<string>, pathInfo2:string, chunkId:number, filter?:string):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:string, pathInfo2:Array<string>, chunkId:number, filter?:RegExp):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:string, pathInfo2:Array<string>, chunkId:number, filter?:string):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:Array<string>, pathInfo2:Array<string>, chunkId:number, filter?:RegExp):Iterator;
    function twoGroupFilePairGenerator(pathInfo1:Array<string>, pathInfo2:Array<string>, chunkId:number, filter?:string):Iterator;

    /**
     * Creates an iterator which reads provided file (specified by path) line by
     * line.
     *
     * @param path A path to a file
     */
    function fileReader(path:string):Iterator;

    /**
     * Creates a new or returns an existing file chunk reader
     * identified by the file path and chunkId.
     *
     * @param path A path to a file we want to read
     * @param chunkId An index of the required chunk (starts from zero)
     * @param chunkSize A chunk size in lines; if omitted then automatic estimation is performed
     * @param startLine The first line to read (0 by default)
     */
    function fileChunkReader(path:string, chunkId:number, chunkSize:number=null, startLine:number=0):Iterator;

    /**
     * Saves a string to a file in a synchronous way
     *
     * @param path
     * @param text a text to be saved
     * @return true on success else false
     */
    function saveText(path:string, text:string):boolean;

    /**
     * Reads whole file into a string. It is intended to read
     * smaller files (e.g. script configuration).
     *
     * @param path a path to a file
     * @return file contents or null in case of an error
     */
    function readText(path):string;

    /**
     * Imports JSON encoded data from a file.
     *
     * @param path A path to a file
     */
    function readJSON(path:string):{[key:string]:any};

    /**
     * A file writer based on Java's BufferedWriter.
     *
     * @param path
     */
    function fileWriter(path:string):FileWriter;

    /**
     * Fetches a content (as a string) via HTTP using GET method.
     *
     * @param url
     */
    function httpGet(url:string):string;

    /**
     * Calculates a hash value of an object based on passed algorithm name
     *
     * @param s A string to be hashed (or any object with reasonable toString() conversion)
     * @param algorithm One of {md5, sha1, sha256, sha384, sha512} (values can be also in upper-case)
     * @return hash of provided object (toString() is used to convert it)
     */
    function hash(s:any, algorithm:string):string;

    /**
     * Pauses current worker for t seconds. Fractions of second
     * are permitted (e.g. orzo.sleep(3.7)).
     *
     * @param t A time in seconds
     */
    function sleep(t:number);

    /**
     * Returns a number of available processors. Please note that Intel CPUs with
     * hyper-threading report twice as high as is actual number of physical
     * cores.
     */
    function numOfProcessors():number;

    /**
     * Sorts Java list using provided cmp function. The passed list
     * is sorted (i.e. nothing is returned).
     *
     * @param data a java.util.List compatible data type (which includes JS array)
     * @param {function} cmp
     */
    function sortList<T>(data:Array<T>, cmp:(v1:T, v2:T)=>number):void;

    /**
     * Creates a native JavaScript array. It should be faster than doing this in
     * JavaScript.
     *
     * @param size
     * @return native JavaScript array
     */
    function array<T>(size:number):Array<T>;

    /**
     * Creates a native JavaScript zero-filled array. It should be faster than
     * doing this in JavaScript.
     *
     * @param size
     * @return a JavaScript array
     */
    function zeroFillArray(size:number):Array<number>;

    /**
     * Creates a numeric matrix of a specified size. Returned value
     * is a normal JavaScript array (of arrays).
     *
     * @param width
     * @param height
     * @returns 2-D matrix
     */
    function numericMatrix(width:number, height:number):Array<Array<number>>;

    /**
     * Creates an array of numbers starting from 'from' with increment 1
     * up to 'to' - 1.
     *
     * @param from
     * @param to
     */
    function range(from:number, to:number):Array<number>;

    /**
     * Creates an array of numbers starting from zero with increment 1
     * up to 'to' - 1.
     *
     * @param to
     */
    function range(to:number):Array<number>;

    /**
     * Creates an array of unique items out of an existing array. In case values
     * to be compared are wrapped in a structured data type, an optional access
     * function can be passed. Internally, a Java HashSet type is used here.
     *
     * @param data
     * @param getValue
     */
    function uniq<T>(data:Array<any>, getValue:(v:any)=>T);
    function uniq<T>(data:Array<T>);

    /**
     * Converts 2D numeric matrix into a 1D vector; line-by-line
     *
     * @param matrix A 2D matrix
     * @returns {array} output 1D vector
     */
    function flattenMatrix<T>(matrix:Array<Array<T>>):Array<T>;

    /**
     * Measures the execution time of the provided function. Please note that in
     * case of asynchronous code you may not obtain the value you have been
     * expecting.
     *
     * @param fn A function to be measured
     * @return time in milliseconds
     */
    function measureTime(fn:(v:any)=>any):number;

    /**
     * Renders a Jade template with custom CSS file
     *
     * @param templatePath
     * @param cssPath
     * @param data Template data
     * @return resulting HTML
     */
    function renderTemplate(templatePath:string, cssPath:string, data:{[k:string]:any}):string;

    /**
     * Loads a bitmap picture from filesystem.
     * Please note that the returned Image is always greyscale.
     *
     * @param path
     */
    function loadImage(path:string):Image;

    /**
     * Provides Python-like 'with' guarded block which allows running
     * a code with a closeable resource. The resource is closed in the
     * end even if there is an error during the call.
     *
     * @param obj An object we want to work on
     * @param fn A function wrapping the actions we want to perform on the object
     * @param err A function to be called in case of an exception
     */
    function doWith(obj:Closeable, fn:(v:Closeable)=>void, err:(e:Error)=>void):void;

}

/**
 * Functions related to processing of HTML pages.
 */
declare module orzo.html {
    /**
     * Parses an HTML source code
     *
     * @param html
     */
    function parseHTML(html:string):Document;

    /**
     * Loads a web page from a specified URL. Page is loaded using GET method.
     *
     * @param url
     */
    function loadWebsite(url:string):Document;

    /**
     * Finds all the elements matching CSS select query starting from rootElement and
     * applies a callback to each element.
     *
     * @param rootElement
     * @param query
     * @param fn
     */
    function query(rootElement:Element, query:string, fn:(item:Element)=>void):void;

    /**
     * Similar to query() but returns all the matching elements instead.
     * @param rootElement
     * @param query
     */
    function find(rootElement:Element, query:string):Array<Element>;
}

/**
 * Registers a MAP operation
 *
 * @param callback A function serving as a MAP
 */
declare function map<T>(callback:(v:T)=>void):void;

/**
 *
 * @param callback
 */
declare function applyItems(callback:(dataChunk:any, map:MapFunction)=>void):void; // TODO T?

/**
 * Registers a function specifying how data chunks (= files, parts of a single file etc.)
 * are defined/created.
 *
 * @param numWorkers
 * @param applyFn A function which defines how idx-th chunk looks like
 */
declare function dataChunks(numWorkers:number, applyFn:(idx:number)=>void):void;

/**
 * Registers a REDUCE operation
 *
 * @param numWorkers
 * @param fn
 */
declare function reduce(numWorkers:number, fn:(key:string, values:Array<any>)=>void):void;

/**
 * Emits a value in MAP & REDUCE operations
 *
 * @param key
 * @param value
 */
declare function emit(key:string, value:any):void;

/**
 *
 * @param resultsFn
 */
declare function finish(resultsFn:(results:Results)=>void):void;

/**
 * A convenience function to instantiate a datalib.Data object
 *
 * @param d
 * @param getItem
 */
declare function D<T>(d:Array<T>, getItem?:(v:T)=>number):datalib.Data<T>;

/**
 * A general Iterator factory function
 *
 * @param data
 * @param next
 * @param hasNext
 */
declare function iterator<T>(data:Array<any>, next:(item:any)=>T, hasNext:(item:any)=>boolean):Iterator;

export = orzo;