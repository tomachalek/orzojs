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


interface IResults {

    /**
     * Iterates over emitted keys (without defined order)
     * and calls the passed function with key and
     * its respective values as arguments.
     *
     * @param fn
     */
    each(fn:(key:string, values:Array<any>)=>void):void;

    /**
     * Returns all the values emitted with the passed key.
     *
     * @param key
     */
    get(key:string):Array<any>;

    /**
     * Tests whether the passed key has been emitted.
     *
     * @param key
     */
    contains(key:string):boolean;

    /**
     * Returns a list (without defined order) of emitted keys
     */
    keys():Array<string>;
}


interface SortedResults extends IResults {

    /**
     * Iterates over alphabetically sorted emitted keys
     * and calls the passed function with key and
     * its respective values as arguments.
     *
     * @param fn
     */
    each(fn:(key:string, values:Array<any>)=>void):void;

    /**
     * Returns an alphabetically sorted list of emitted keys
     */
    keys():Array<string>;
}


/**
 *
 */
interface Results extends IResults {
    sorted:SortedResults;
}

/**
 * Any object with 'close' method. This is typically used
 * along with "doWith" and file or Web access handlers.
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
 * Iterates over a file line by line.
 */
interface FileIterator extends Iterator<string>, Closeable {
    path:string;
}

/**
 *
 */
interface StringDistances {

    /**
     * Find the Levenshtein distance between two Strings.
     * This is the number of changes needed to change one String into another,
     * where each change is a single character modification (deletion, insertion
     * or substitution).
     *
     * Uses org.apache.commons.lang3.StringUtils.getLevenshteinDistance
     *
     * @param s1
     * @param s2
     */
    levenshtein(s1:string, s2:string):number;

    /**
     * Find the Fuzzy Distance which indicates the similarity score between two Strings.
     * One point is given for every matched character. Subsequent matches yield two bonus points.
     * A higher score indicates a higher similarity.
     *
     * Uses org.apache.commons.lang3.StringUtils.getFuzzyDistance
     *
     * @param s1
     * @param s2
     * @param locale
     */
    fuzzy(s1:string, s2:string, locale:string):number;

    /**
     * Find the Jaro Winkler Distance which indicates the similarity score between two Strings.
     * The Jaro measure is the weighted sum of percentage of matched characters from each file
     * and transposed characters. Winkler increased this measure for matching initial characters.
     *
     * Uses org.apache.commons.lang3.StringUtils.getJaroWinklerDistance
     *
     * @param s1
     * @param s2
     */
    jaroWinkler(s1:string, s2:string):number;

    /**
     * Normalized compression distance using GZIP algorithm.
     *
     * Based on: https://en.wikipedia.org/wiki/Normalized_compression_distance#Normalized_compression_distance
     *
     * @param s1
     * @param s2
     */
    normalizedCompression(s1:string, s2:string):number;
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
        correl<T>(other:Data<T>):number;
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
interface FileWriter extends Closeable {

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
interface Env {

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
    inputArgs:Array<string>;

    /**
     * A unique number between 0...[num_workers - 1] specifying ID
     * of a current worker. The assignment is done automatically.
     */
    workerId:number;

    /**
     * A filesystem or a Java resource path of user's script.
     */
    scriptName:string;

    /**
     * A current working directory (the one Orzo.js has been started from).
     */
    cwd:string;

    /**
     * A UNIX time specifying when the calculation started.
     */
    startTimestamp:number;

}

/**
 * This is actual "singleton" instance of Env provided by Orzo.js
 */
declare var env:Env;


/**
 * REST-client methods
 */
interface RestMethods {

    delete(url:string):string;

    get(url:string):string;

    head(url:string):string;

    post(url:string, body:string):string;

    put(url:string, body:string):string;
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
    function printf(s:string, ...values:any[]);

    /**
     * Formats a string by placing provided values into respective reference
     * placeholders. Works just like Java's String.format().
     *
     * @param s A string to be printed
     * @param values Values to replace formatting placeholders in the string
     */
    function sprintf(v:string, ...values:any[]):string;

    /**
     * Prints internals of a passed object. This is intended for debugging purposes.
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
    function directoryReader<T>(pathInfo:string|Array<string>, chunkId:number,
          filter?:RegExp|string):Iterator<T>;

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
    function filePairGenerator<T>(pathInfo:string|Array<string>, chunkId:number,
          filter?:RegExp|string):Iterator<T>;

    /**
     * Generates a Cartesian product of two groups of files.
     *
     * @param pathInfo1 A path or paths defining the first group
     * @param pathInfo2 A path or paths defining the second group
     * @param chunkId For what chunk the we are producing the subset of the whole set AxB
     * @param filter If non-empty then only files matching the provided value will be included
     */
    function twoGroupFilePairGenerator<T>(pathInfo1:string|Array<string>, pathInfo2:string|Array<string>,
                                       chunkId:number, filter?:RegExp|string):Iterator<T>;

    /**
     * Creates an iterator which reads provided file (specified by path) line by
     * line.
     *
     * @param path A path to a file
     */
    function fileReader(path:string):FileIterator;


    /**
     * Creates an iterator which reads provided gzipped file line by line.
     *
     * @param path A path to a gzipped file
     */
    function gzipFileReader(path:string):FileIterator;

    /**
     * Creates a new or returns an existing file chunk reader
     * identified by the file path and chunkId.
     *
     * @param path A path to a file we want to read
     * @param chunkId An index of the required chunk (starts from zero)
     * @param chunkSize A chunk size in lines; if omitted then automatic estimation is performed
     * @param startLine The first line to read (should be 0 by default)
     */
    function fileChunkReader<T>(path:string, chunkId:number, chunkSize?:number,
          startLine?:number):Iterator<T>;

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
    function readTextFile(path):string;

    /**
     * Imports JSON encoded data from a file.
     *
     * @param path A path to a file
     */
    function readJsonFile<T>(path:string):T;

    /**
     * Converts an object to JSON. In Orzo.js, this is ofter better
     * than JSON.stringify() which may return 'undefined' in some situations
     * (see https://github.com/tomachalek/orzojs/issues/22)
     *
     * @param obj An object to be converted
     */
    function toJson(obj:any):string;

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
     * Loads a bitmap picture from filesystem.
     * Please note that the returned Image is always greyscale.
     *
     * @param path
     */
    function loadImage(path:string):Image;

    var stringDistance:StringDistances;

    var rest:Rest;
}

/**
 * Provides a set of common hash functions.
 */
declare module orzo.hash {

    function md5(s:{}|string):string;

    function sha1(s:{}|string):string;

    function sha256(s:{}|string):string;

    function sha384(s:{}|string):string;

    function sha512(s:{}|string):string;
}

declare module orzo.fs {
    /**
     * Recursively deletes all the entries from the directory.
     * The directory itself is preserved.
     *
     * @param path A path to a directory
     */
    function cleanDirectory(path:string):void;

    /**
     * Returns the last modification time of a file
     * (UNIX time in milliseconds).
     */
    function getLastModified(path:string):number;

    /**
     * Returns a size of a file (in bytes)
     */
    function getSize(path:string):number;

    /**
     * Moves a file to a specified destination which
     * can be either a (non-existing) file or a directory.
     */
    function moveFile(srcPath:string, dstPath:string):void;

    /**
     * Tests whether a file/directory exists
     */
    function exists(path:string):boolean;
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
 * Performs a MAP operation
 */
interface MapFunction<T> {
    (v:T):void;
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
declare function applyItems<T>(callback:(dataChunk:any, map:MapFunction<T>)=>void):void;

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
declare function iterator<T>(data:Array<any>, next:(item:any)=>T, hasNext:(item:any)=>boolean):Iterator<T>;

/**
* Provides a convenient way how to work with closeable resource(s). These
* are always guaranteed to be closed even if the passed function throws an
* error. The error can be still processed via an optional callback.
* In case more than one resource is passed objects are closed in reversed
* order (e.g. doWith([r1, r2], function () {}) closes r2 first then r1).
*
*
* @param obj An object(s) we want to work on
* @param fn A function wrapping the actions we want to perform on the object
* @param err A function to be called in case of an exception
*/
declare function doWith<T extends Closeable>(obj:T|Array<T>, fn:(v:T)=>void, err?:(e:Error)=>void):void;

/**
 * Loads a module. Orzo.js supports only sandboxed module loading.
 * Searched paths are:
 *   1) main script working directory
 *   2) a directory specified by -m parameter
 *
 * @param moduleId
 */
declare function require<T>(moduleId:string):T;