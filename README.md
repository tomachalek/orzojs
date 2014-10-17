Orzo.js
=======

Orzo.js is a simple multi-threaded Map-Reduce implementation written in Java and
programmable in JavaScript.

Introduction
------------

*Orzo.js* is designed to run on a single (yet multi-core/multi-CPU) machine which is the reason why
it does not create a thread for each input data item you want to apply a *map* function to. Instead, *chunks of data items are processed in parallel*. Such approach requires you to tell the *Orzo.js* how to split the data into these chunks or how the data is already split. Sometimes it can be easy - for example a list of log files where each file represents a single chunk and each line represents a data item. Some cases are more complicated - e.g. if you have one big text file you want to be processed in parallel. Fortunatelly, *Orzo.js* provides some useful functions to help you in such situations.

When your script's execution starts, *Orzo.js* attaches each data chunk to its own worker thread and starts the calculation by applying *map* function to each data item within these chunks. Once all the particular results are available, new worker threads are created to perform the *reduce* operation. After that, the *finish* operation is executed to allow custom finalization (e.g. presentation of the result, data export, clean-up etc.).

Orzo.js comes with some predefined functions you can use to perform your calculations. You can also define your own modules or use 3rd party ones because Orzo.js supports CommonJS compliant module importing.

How to build the application from source
----------------------------------------

To build *Orzo.js* from sources, you need the JDK 6+, a working Maven installation and an Internet connection to be able to download required dependencies (Maven does it automatically). In the directory where *Orzo.js* sources are located (*pom.xml* file should be there) type:

```bash
mvn clean dependency:copy-dependencies package
```

How to write a script
---------------------

Orzo.js' map-reduce scripts are written in JavaScript. You do not have to worry about the whole 
calculation workflow - it is taken care of by the application. All you have to do is to register several obligatory functions with predefined signature:

* dataChunks,
* applyItems,
* map,
* reduce,
* finish. 

During each calculation phase, Orzo.js calls your function and applies it on 
currently processed data.

### Example problem

Let's say we have 10 files where each line represents a valid JSON-encoded data. First of 
all, we define a simple helper function to read the content of the files:

```js
function loadLogFile(path) {
    var f = orzo.fileReader(path),
    ans = [];

    while (f.hasNext()) {
        ans.push(JSON.parse(f.next()));
    }
    return ans;
}
```

Then we register the function *dataChunks*. It tells the Orzo.js how our data is split (or how
we want to split them) and how many such 'chunks' are there. Orzo.js creates a worker thread
for each of the chunks. The following example shows we have 10 chunks (here a chunk == a file)
and how to read them (here by reading files *server.0.log*, *server.1.log*,..., *server.9.log*).

```js
dataChunks(10, function (idx) {
    return loadLogFile('server.' + idx + '.log');
});
```

The second registered function, *applyItems* is also Orzo.js-specific and it defines a relationship
between a *data chunk* and *data item* where *data item* will be actually the value passed to
the *map* function. The relationship can be 1:1 - which would mean each worker would process just
one *map* function and one file in this example. But with limited computer multi-threading and IO capacity, a relationship 1:N would be more likely. In such case each *data chunk* will produce *N* 
*data items* and hence *N* *map* function calls.

```js
applyItems(function (dataChunk, map) { 
    while (dataChunk.hasNext()) {
        map(dataChunk.next());
    } 
});
```

Now we can define the two essential functions - *map* and *reduce*. Let's say our log lines
contain the *errorType* attribute (with possible values *WARNING*, *RECOVERABLE*, *FATAL*) 
and we want to calculate their total occurrences:

```js
map(function (dataItem) {
    emit(dataItem.errorType, 1); // for each type we emit 1
});
```

The reduce function will just calculate the sum of all emitted 1s for all the keys. 
Orzo.js' reduce function has actually two signatures - there is an optional first (numeric) 
parameter which can specify how many worker threads should Orzo.js create. If omitted then 
the same number as in case of *dataChunks* is used. 

Generally, the question how many worker threads to specify depends on the data and the problem 
solved. If you emit the same key for all the data then multi-threading brings no advantage in case of Orzo.js. 

Applied to our example, we know we have actually three error types we want to count
so we can make Orzo.js to sum each type independently:

```js
reduce(3, function (key, values) {
    emit(D(values).sum());
});
```
(Here we used a special function [datalib.D](API.md#api_datalib_D) which wraps your data and offers some basic statistical functions)

Finally, we want to see the result which is exactly why there is a *finish* function:

```js
finish(function (results) {
    results.each(function (key, values) {
        // our reduce emits just a single value but Orzo.js always
        // creates an array which means we have to handle 'values'
        // as an array
        orzo.printf('%s -> %d occurrences\n', key, values[0]);
    });    
});
```

Further documentation
---------------------

Please refer to the [API documentation](API.md)

