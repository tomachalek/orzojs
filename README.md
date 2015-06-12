Orzo.js
=======

Orzo.js is a simple **multi-threaded, in-memory Map-Reduce** implementation written in Java and **programmable in JavaScript**. It can be used as an <acronym title="Extract, Transform, Load">ETL</acronym> tool, educational software, "smaller than big data" processing/analyzing tool, prototyping tool etc.

*Orzo.js* is designed to run on a single multi-core/multi-CPU machine. If you know how [Map-Reduce](https://en.wikipedia.org/wiki/MapReduce) works, the only additional think to do is to decide how your data can be split into chunks. Orzo.js does not create a thread for each *map* operation as it would cause a big overhead. It instead processes multiple chunks of data in parallel.

Orzo.js comes with a bunch of predefined functions you can use to perform your tasks and [analyze your data](http://www.orzojs.org/API.md#api_datalib_Data). You can also create your own or use 3rd party CommonJS modules. 

To get more information plese visit [www.orzojs.org](http://www.orzojs.org/).


How to start
------------

When writing a script you don't have to even start from scratch:

```
orzojs -t myscript.js
```

This command will create a template of your script along with TypeScript *d.ts.* file for comfortable editing in JavaScript IDE (Webstorm, Visual Studio Code, ...).


How to process data
-------------------

You do not have to worry about the whole calculation workflow - it is taken care of by the application. All you have to do is to **register several obligatory functions**:

* [dataChunks](http://www.orzojs.org/API.md#api_dataChunks),
* [applyItems](http://www.orzojs.org/API.md#api_applyItems),
* [map](http://www.orzojs.org/API.md#api_map),
* [reduce](http://www.orzojs.org/API.md#api_reduce),
* [finish](http://www.orzojs.org/API.md#api_finish). 

During each calculation phase, Orzo.js calls your function passing it the appropriate data.

Example
-------

Let's say we have 10 log files (*server.0.log*, *server.1.log*, ..., *server.9.log*). Each line of the log file  represents a valid JSON-encoded data. 

### Chunking the data

We have to decide how we define a chunk. In our case it may be a single file or a list of files. We could define this manually but orzo offers a convenient function for this: [orzo.directoryReader](http://www.orzojs.org/API.md#api_orzo_directoryReader). This function will automatically walk through provided directory (or directories), apply optional filter and create a predefined number of path groups.

  
```js
dataChunks(10, function (idx) {
    return orzo.directoryReader('./data, idx);
});
```

With this code we have told Orzo.js that:

  1. there will be 10 chunks (=> 10 threads)
  2. once it requires a chunk with ID = [idx], then it should use a directory reader which is expected to return its [idx]-th file group.

Now we have to specify how the *map* function will be applied on a chunk. Our *directoryReader* returns an *Iterator* which we will use in the following code. To be able to process a concrete file, we have to read it somehow. Orzo.js offers a simple line-by-line reader [orzo.fileReader](http://www.orzojs.org/API.md#api_orzo_fileReader):


```js
applyItems(function (fileList, map) {
    var fr; 
    while (fileList.hasNext()) {
        fr = orzo.fileReader(fileList.next()); // next file path in a group
        while (fr.hasNext()) {   
            map(fr.next()); // next line in a file
        }
    }
});
```

### Map-Reduce

Now we can define *map* and *reduce* functions. Let's say our JSON log lines contain the *errorType* attribute (with possible values *WARNING*, *RECOVERABLE*, *FATAL*) and we want to calculate their total occurrences. We know what our *map* will obtain from Orzo.js from the previous code block:

```js
map(function (logLine) {
    var parsed = JSON.parse(logLine);
    emit(parsed.errorType, 1); // for each type we emit 1
});
```

In our case the *reduce* function will just calculate a sum of all emitted *1*s for all the different keys (*WARNING*, *RECOVERABLE*, *FATAL*). Orzo.js reduce function has actually two signatures - there is an optional first (numeric) parameter which can specify how many worker threads should Orzo.js create. If omitted then the same number as in case of *dataChunks* is used.  

Let's say 2 threads are enought here:

```js
reduce(3, function (key, values) {
    emit(D(values).sum());
});
```
(Here we have used a special factory function [datalib.D](http://www.orzojs.org/API.md#api_datalib_D) which wraps our data can apply some basic statistical functions on it).

Finally, we want to print the result:

```js
finish(function (results) {
    results.sorted.each(function (key, values) {
        // our reduce emits just a single value but Orzo.js always
        // creates an array which means we have to handle 'values'
        // as an array
        orzo.printf('%s -> %d occurrences\n', key, values[0]);
    });    
});
```

Download
--------

Please visit Orzo.js [download page](http://www.orzojs.org/downloads.md).

You can also build *Orzo.js* from sources. If you have JDK 8+ and a working Maven installation just type the following commands in Orzo.js root directory:

```bash
mvn clean dependency:copy-dependencies package
```

Further documentation
---------------------

Please refer to the [API page](http://www.orzojs.org/API.md) on Orzo's website.

