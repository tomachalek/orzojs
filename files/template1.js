// /////////////////////////////////////////////////
// A minimalist working template of a Orzo.js script.
// Additional command line arguments are accessible
// in env.inputArgs array.
// /////////////////////////////////////////////////

(function () {
    'use strict';

    /*
     * Here we specify how data chunks are defined and how
     * many chunks are there (4)
     */
    dataChunks(4, function (idx) {
        return orzo.fileChunkReader(env.inputArgs[0], idx);
    });

    /*
     * Now we specify the relationship between a single chunk of data
     * and the map() function.
     */
    applyItems(function (dataChunk, map) {
        while (dataChunk.hasNext()) {
            map(dataChunk.next());
        }
    });

    /*
     * Here, the map() function is defined. It is expected to
     * emit a key and a value.
     */
    map(function (data) {
        if (data.length > 0) {
            emit(data[0], 1);
        }
    });

    /*
     * Here we define the reduce() function. It is expected
     * to emit a single value (of any, even a complex type).
     * Optionally, we can define number of parallel workers
     * (6 here).
     */
    reduce(6, function (key,  values) {
        emit(key, D(values).size());
    });

    /*
     * Finish function is run once all the data is processed.
     * Values emitted by the reduce() function are available as
     * an Array.
     */
    finish(function (results) {
        doWith(orzo.fileWriter('results.txt'), function (fw) {
            results.each(function (key, values) {
                fw.writeln(orzo.sprintf('Number of lines starting with "%s": %s\n', key, values[0]));
            });
        });
    });

}());