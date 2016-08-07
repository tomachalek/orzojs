/*
 * An automatically generated Orzo.js task template
 */

/// <reference path="./orzojs.d.ts" />

(function () {
    'use strict';

    var numChunks = 4;
    var numReduceWorkers = 4;
    var outputFile = './results.txt';
    // input file is read from the command-line (env.inputArgs[0])

    dataChunks(numChunks, function (idx) {
        return orzo.directoryReader(env.inputArgs[0], idx);
    });

    processChunk(function (fileList, map) {
        var fr;
        while (fileList.hasNext()) {
            fr = orzo.fileReader(fileList.next());
            while (fr.hasNext()) {
                map(fr.next());
            }
        }
    });

    map(function (data) {
        var parsed;

        try {
            parsed = JSON.parse(data);
            emit(parsed.id, 1);

        } catch (error) {
            emit('error', 1);
        }
    });

    reduce(numReduceWorkers, function (key,  values) {
        emit(key, D(values).size());
    });

    finish(function (results) {
        doWith(
            orzo.fileWriter(outputFile),
            function (fw) {
                results.each(function (key, values) {
                    fw.writeln(orzo.sprintf('Number of lines starting with "%s": %s\n',
                               key, values[0]));
                });
            },
            function (err) {
                orzo.printf('Failed to process the result: %s\n', err);
            }
        );
    });

}());