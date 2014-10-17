Orzo.js - programming reference
===============================

* [require(moduleName)](#api_require)
* [doWith(obj, fn[, errCallback])](#api_doWith)
* [dataChunks(numChunks, factoryFn)](#api_dataChunks)
* [applyItems(fn)](#api_applyItems)
* [emit(key, data)](#api_emit)
* [finish(fn)](#api_finish)
* [map(fn)](#api_map)
* [reduce([numWorkers, ]fn)](#api_reduce)
* [orzo](#api_orzo)
	* [.print(text)](#api_orzo_print)
	* [.printf(text)](#api_orzo_printf)
	* [.sprintf(text) -> string](#api_orzo_sprintf)
	* [.fileChunkReader(path, chunkId, chunkSize=null, startLine=0) -> BaseIterator](#api_orzo_fileChunkReader)
	* [.directoryReader(path, chunkId[, filter]) -> BaseIterator](#api_orzo_directoryReader)
	* [.filePairGenerator(path, chunkId[, filter]) -> BaseIterator](#api_orzo_filePairGenerator)
	* [.saveText(path, text)](#api_orzo_saveText)
	* [.fileWriter(path)](#api_orzo_fileWriter)
	* [.md5(str) -> string](#api_orzo_md5)
	* [.array(size) -> array](#api_orzo_array)
	* [.zeroFillArray(size) -> array](#api_orzo_zeroFillArray)
	* [.flattenMatrix(arrayOfArrays) -> array](#api_orzo_flattenMatrix)
	* [.measureTime(fn) -> number](#api_orzo_measureTime)
	* [.report(outputPath) -> Report](#api_orzo_report)
* [FinalResults](#api_FinalResults)
	* [.each(\[sortKeys, \]fn)](#api_FinalResults_each)
	* [.get(key) -> array](#api_FinalResults_get)
	* [.contains(key) -> boolean](#api_FinalResults_contains)
	* [.keys() -> array](#api_FinalResults_keys)
* [Report](#api_Report)
	* [.title(\[title\])](#api_Report_title)
	* [.cols(\[columns\])](#api_Report_cols)
	* [.addRow(rowData)](#api_Report_addRow)
	* [.close()](#api_Report_close)
* [env](#api_env)
* [datalib](#api_datalib)
	* [datalib.D(data\[, itemAccessFn\]) -> Data](#api_datalib_D)
	* [Data](#api_datalib_Data)
		* [.size() -> Number](#api_datalib_Data_size)
		* [.get(i) -> Number](#api_datalib_Data_get)
		* [.sum() -> Number](#api_datalib_Data_sump)
		* [.average() -> Number](#api_datalib_Data_average)
		* [.stdev() -> Number](#api_datalib_Data_stdev)
		* [.correl(otherData) -> Number](#api_datalib_Data_correl)


<a name="api_require"></a>

require(moduleName)
-------------------

Import a CommonJS module. 

```js
var datetime = require('myDatetimeLib');

datetime.doSomethingCool();
```

Please note that Orzo runs the module functionality in so called *sandboxed* mode (see e.g. [CommonJS Modules/1.1 spec.](http://wiki.commonjs.org/wiki/Modules/1.1) or [org.mozilla.javascript.commonjs.module](https://github.com/mozilla/rhino/blob/master/src/org/mozilla/javascript/commonjs/module/Require.java) documentation). It means that no paths in module names are allowed (require('/path/to/module') won't work).

By default, Orzo sets import path for modules as a dirname of the executed script. I.e. if you run the script */home/joe/work/calc.js* then any module in */home/joe/work* can be included.

If you need to include some additional location you can run Orzo with parameter *-m*:

```
java -jar orzo.jar -m /usr/local/lib /home/joe/work/calc.js
``` 

When implementing an Orzo module, please keep in mind that there is no browser-like API available (no *window* object, no *DOM*, no *console*,...).



<a name="api_doWith"></a>

doWith(obj, fn[, errCallback])
------------------------------

Works in a similar way to [Python's with](https://docs.python.org/2/reference/compound_stmts.html#with) statement. The function ensures that used object will be closed even in case of an error. You can also pass an error handler function to access possible thrown error. It is ok to use also an object without the *close()* method.

#### arguments:

* **obj** - object to work with
* **fn** - a function with signature **function(obj)** wrapping required operation on **obj**
	* in the function, **this** keyword can be used instead of **obj** argument as they refer to the same object
* **errCallback** - an optional function called in case an error occurs within **fn**


```js
doWith(
	orzo.fileReader('foo.txt'), 
	function (r) {
    	while (r.hasNext()) {
    	    orzo.print(this.next()); // this === r
    	}
	},
	function (err) {
		orzo.printf('something went wrong: %s\n', err); 
	}
);
```

<a name="api_dataChunks"></a>

dataChunks(numChunks, factoryFn)
--------------------------------

This function registers two important things:

* a number of chunks input data will be split into; it is the same number that will be 
processed in parallel,
* a factory function for creating the chunks.

The factory function can be understood in this way: "If Orzo.js asks me for i-th chunk, how do I respond?". 
If you use predefined data access functions ([fileChunkReader](#api_orzo_fileChunkReader), 
[directoryReader](#api_orzo_directoryReader) or
[filePairGenerator](#api_orzo_filePairGenerator)) then you just pass them the *idx* argument and
they take care of the rest (see the example below).

#### arguments:

* **numChunks** - specifies how many chunks will be created
* **factoryFn** - a function with signature **function(idx)** returning an object representing a chunk with index **idx**

#### returns:

* The function may return any data type you want to use in your [applyItems](#api_applyItems) function. 
Orzo.js itself does not really care what is inside the data.


```js
dataChunks(10, function (idx) {
    // here we rely on the fact that directoryReader knows what "idx" is
    return orzo.directoryReader('./my-data', idx);  
});
```

The following code shows an example of data chunk factory not relying on automated data reader functions:

```js
dataChunks(12, function (idx) {
    var files = [],
        months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    
    for (i = 0; i < 100; i += 1) {
        files.push('/my/logs/' + months[idx] + '/dataset-' + i + '.log');
    }
    return files; 
    // ok, so my applyItems callback will have to process a list of strings (file paths)
});
```

<a name="api_applyItems"></a>

applyItems(fn)
--------------

Registers a function describing how to access a single data item when iterating over a data chunk. In other words, it maps one granularity (defined by the number of data chunks) onto another (defined by a "atomic" data item the *map* function will be applied to).

#### arguments:

* **fn** a function with signature **function(dataChunk, map)** where **dataChunk** is an object produced by a factory function (see [dataChunks](#api_dataChunks)).

```js
applyItems(function (dataChunk, map) {
    while (dataChunk.hasNext()) {
        map(dataChunk.next());
    }  
});
```

<a name="api_emit"></a>

emit(key, data)
---------------

This function is used in both *map* and *reduce* functions to pass required data
to the next processing phase. 

#### arguments:

* **key** - a key emitted data is bound to; please note it must be always a string which means in case of complex types, a conversion must be performed (e.g. using orzo.sprintf() or JSON.stringify())
* **data** - data to be emitted; any type is allowed

<a name="api_finish"></a>

finish(fn)
----------

Allows performing actions after all the calculations are finished. This is best suited for final data and result adjustment and export or presentation. This operation runs in the main thread.

#### arguments:

* **fn** - a function with signature **function(results, info)**
	* **results** is a [FinalResults](#api_FinalResults) instance
	* **info** is a simple key->value object with following properties:
		* **duration** - a floating point number specifying the duration of the calculation in seconds
		* **datetime** - a date and time when the calculation finished (format *yyyy-mm-dd hh:mm:ss*)  


```js
finish(function (results, info) {
    orzo.printf('date: %s\n', info.datetime);
	orzo.printf('duration: %s\n', info.duration);
	results.each(function (...) {
		...
	});
});
```

<a name="api_map"></a>

map(fn)
-------

Registers the *map* function. By calling *emit(key, value)*, intermediate result is registered for further processing.

#### arguments:

* **fn** a function with signature **function(data)**; the form of passed data is given by what is registered 
via [applyItems](#api_applyItems).

```js
map(function (serverRequest) {
    emit(serverRequest.day, 1);  
});
```

<a name="api_reduce"></a>

reduce([numWorkers, ]fn)
------------------------

Registers the *reduce* function. By calling *emit(key, value)* function you add the *value* to the result list attached to the *key*.

#### arguments:

* **numWorkers** - how many worker threads will be created to perform the *reduce* phase; if omitted then the same number will be used as in [dataChunks](#api_dataChunks).
* **fn** - a function with signature **fn(key, values)**; all the data emitted during the *map* phase under the key **key** is stored in the **values** argument 

```js
reduce(function (key,  values) {
  
});

// or

reduce(numWorkers, function (key,  values) {
  
});
```

<a name="api_BaseIterator"></a>

BaseIterator
------------

### BaseIterator.hasNext() -> boolean

Tests whether the instance has another item to return.


### BaseIterator.next() -> object

Returns next item. If there is no item, *undefined* is returned.


<a name="api_orzo"></a> 

orzo
----

This object represents a core functions library (like writing to stdout/stderr, handling files, logging etc.).

<a name="api_orzo_print"></a>

### orzo.print(text)

Prints a string to the standard output. Structured objects are not guaranteed to be displayed with all their internals. 

<a name="api_orzo_printf"></a>

### orzo.printf(text)

Prints a formatted string to the standard output. For details see Java [String.format](http://docs.oracle.com/javase/7/docs/api/java/lang/String.html#format(java.lang.String,%20java.lang.Object...)).

<a name="api_orzo_sprintf"></a>

### orzo.sprintf(text) -> string

Just like [orzo.printf(text)](#api_orzo_printf) but this function returns a string instead of printing it.

<a name="api_orzo_fileReader"></a>

### orzo.fileReader(path) -> BaseIterator

Opens a file specified by its path and creates a line iterator (BaseIterator).

#### arguments:

* **path** - path to a file

<a name="api_orzo_fileChunkReader"></a>

### orzo.fileChunkReader(path, chunkId, chunkSize=null, startLine=0) -> BaseIterator

Opens a file and returns an iterator to read file's chunk. Please note that there is no
guarantee that the chunk is continuous in terms of original data. 

The convenient thing about this function is that you do not have to specify how to generate 
i-th chunk (i.e. what actually *chunkId* argument means). The function is aware about total 
number of chunks and provides proper data automatically. 

Using this function, it is possible to read a big file by multiple worker threads. E.g. if you 
define 10 data chunks then each worker thread will process 1/10 of the input file and generate 
respective number of *map* calls on them (this depends on how you specify the *applyItems* function).


#### arguments:

* **path** - path to a file we want to process
* **chunkId** - a chunk we want to obtain; this is typically passed from the *dataChunks* callback's argument *chunkId*
* **chunkSize** - in case we want to set a chunk size manually, otherwise *null* should be used to trigger auto-mode
* **startLine** in case we want some initial lines to be omitted

<a name="api_orzo_directoryReader"></a>

### orzo.directoryReader(path, chunkId[, filter]) -> BaseIterator

Creates a list of files in the specified directory (including all subdirectories), splits the list 
into chunks and returns an iterator (with *next()*, *hasNext()* methods) with specified index. 
This can be used to process directory containing many files we want to process in a parallel way.

#### arguments:

* **path** - a path of a directory to be searched; also an array of paths is accepted
* **chunkId** - a chunk we want to obtain; this is typically passed from the *dataChunks* callback's argument *chunkId*
* **filter** - an optional regexp to specify files we want to include in the result

<a name="api_orzo_filePairGenerator"></a>

### orzo.filePairGenerator(path, chunkId[, filter]) -> BaseIterator

Reads files in a directory (just like [directoryReader](#api_orzo_directoryReader)) and generates all the file pairs 
without repeating (two instances of the same file are not considered a pair). For example if the files 
are *foo.txt*, *bar.xml* and *test.pdf*, then *[foo.txt, bar.xml]*, *[foo.txt, test.pdf]* 
and *[bar.xml, test.pdf]* are produced (in general: N*(N-1)/2 pairs is generated). 
Order of items in a pair is not guaranteed (i.e. maybe you will get [a, b], maybe [b, a]).

This can be used e.g. to compare files in some way, search for best matches etc.

#### arguments:

* **path** - a path of a directory to be searched; also an array of paths is accepted
* **chunkId** - a chunk we want to obtain; this is typically passed from the *dataChunks* callback's argument *chunkId*
* **filter** - an optional regexp to specify files we want to include in the result

<a name="api_orzo_saveText"></a>

### orzo.saveText(path, text)

Saves a text to a file in a single batch.

<a name="api_orzo_fileWriter"></a>

### orzo.fileWriter(path)

Creates a buffered text file writer. It is best suitable if you want to write 
larger amount of data piece by piece (otherwise the "saveText(path, text)" 
is a good alternative). 

```js
// returns following object:
{
  write : function (s) {...},
  writeln : function (s) {...},
  close : function () {...}
}
```

<a name="api_orzo_md5"></a>

### orzo.md5(str) -> string

Calculates a *md5* checksum of provided string.

<a name="api_orzo_numOfProcessors"></a>

### orzo.numOfProcessors()


Returns number of processor cores available to the environment. Please note that in case of some Intel CPUs with the *hyper-threading* technology this reports value *2*k* where *k* is number of physical cores.

<a name="api_orzo_array"></a>

### orzo.array(size) -> array

Creates a native Java array wrapped in a JavaScript object. This shows up as a faster solution in most cases than native JavaScript array.

<a name="api_orzo_zeroFillArray"></a>

### orzo.zeroFillArray(size) -> array

Creates a native Java array wrapped in a JavaScript object and filled with zeros. This shows up as a faster solution in most cases than native JavaScript array.

<a name="api_orzo_flattenMatrix"></a>

### orzo.flattenMatrix(arrayOfArrays) -> array

Converts a 2D array into a 1D array. Conversion is performed line by line.


<a name="api_orzo_measureTime"></a>

### orzo.measureTime(fn) -> number

Executes a provided function and measures the time it took to finish. Value is in milliseconds. Please note that in case of asynchronous code this may be misleading.


<a name="api_orzo_report"></a>

### orzo.report(outputPath) -> Report

Creates an instance of the [Report](#api_Report) object.


<a name="api_FinalResults"></a>

FinalResults object
-------------------

This object represents calculation results after reduce phase is finished.

<a name="api_FinalResults_each"></a>

### FinalResults.each([sortKeys, ]fn)

#### arguments:

* **sortKeys** - a boolean specifying whether we want to iterate over sorted keys or not; if omitted then unsorted key list is used
* **fn** - a function with signature **fn(key, values)**
	* **key** is the key emitted during the *reduce* phase
	* **values** are the values emitted along with the **key** 

```js
FinalResults.each(function (key, values) {
    orzo.printf('%s:\n', key);
	values.forEach(function (v) {
		orzo.printf('\t%s\n', v);
	});
});
```

<a name="api_FinalResults_get"></a>

### FinalResults.get(key) -> array

Returns all the values emitted along with the key **key**.

<a name="api_FinalResults_contains"></a>

### FinalResults.contains(key) -> boolean

Tests whether the results contain emitted key *key*.

<a name="api_FinalResults_keys"></a>

### FinalResults.keys() -> array

Returns all the emitted keys.


<a name="api_Report"></a>

Report object
-------------

The *Report* object allows you to output calculation data to an HTML page. It can be conveniently used with *doWith()* function (see the example at the end of the section).

<a name="api_Report_title"></a>

### Report.title([title])

Get or set a report title.

<a name="api_Report_cols"></a>

### Report.cols([columns])

#### arguments:

* **columns** - columns in output table; there are two alternative forms:
	* **Report.cols(column1, column2,...,columnN)**
	* **Report.cols(columnsArray)**

Get or set report data column names.

<a name="api_Report_addRow"></a>

### Report.addRow(rowData)

Report.addRow(col1, col2, ...)
Report.addRow(columnArray)


<a name="api_Report_close"></a>

### Report.close()

Writes the data to a file and closes all the resources (*doWith()* function handles
this for you).

```js
doWith(orzo.report('my-experiment.html'), function (report) {
    report.title('Results of my experiment');
    report.cols('type', 'average time', 'stdev');
    results.each(true, function (key, values) {
	    values.forEach(function (item) {
    		report.addRow(key, item.type, item.averageTime, item.stdev);
        });
    });
});
```

<a name="api_env"></a>

env
--- 

The 'env' object represents calculation environment and provides some of its parameters.

<a name="api_env_workerId"></a>

### env.workerId

Integer identifier of current worker. Values start with zero and end with *dataChunks - 1* (see below)

<a name="api_env_inputArgs"></a>

### env.inputArgs

If a calculation is called with some additional command line parameters (following the script to be executed) these are available as an array here.

<a name="api_env_numChunks"></a>

### env.numChunks

Number of defined chunks. This value is known during calculation phase, not when user script is loaded and executed.

<a name="api_datalib"></a>

datalib
-------

*datalib* is an additional library implemented in pure JavaScript which provides some useful functions.

<a name="api_datalib_D"></a>

### datalib.D(data[, itemAccessFn]) -> Data

#### arguments:

This is a shortcut function to create a [Data](#api_datalib_Data) instance. If individual items are complex structures wrapping values we are interested in, an optional **itemAccessFn** function can be used to unwrap them:

```js
var records = [
  {name : 'John', age : 40},
  {name : 'Jane', age : 37},
  {name : 'Jack', age : 12},
  {name : 'Judy', age : 10}
], ages;

ages = D(records, function (item) { return item.age; });
orzo.print('average age is: %s', ages.average());
```

<a name="api_datalib_Data"></a>

datalib.Data
-------------

This object represents a vector-like data.

<a name="api_datalib_Data_size"></a>

### Data.size() -> Number

Returns number of items within the object.

<a name="api_datalib_Data_get"></a>

### Data.get(i) -> Number

Returns i-th item.

<a name="api_datalib_Data_sump"></a>

### Data.sum() -> Number

Calculates sum of data items.

<a name="api_datalib_Data_average"></a>

### Data.average() -> Number

Calculates arithmetic mean of data items.

<a name="api_datalib_Data_stdev"></a>

### Data.stdev() -> Number

Calculates standard deviation of sample data.

<a name="api_datalib_Data_correl"></a>

### Data.correl(otherData) -> Number

Calculates Pearson product-moment correlation coefficient between caller object and the argument.

#### arguments:

* **otherData** - some other *Data* instance; both must be of the same size, otherwise *NaN* is returned 


