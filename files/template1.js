/*
 * An automatically generated Orzo.js task template
 */

/// <reference path="./orzojs.d.ts" />

(function () {
    'use strict';

    dataChunks(4, function (idx, stateData) {
        return orzo.directoryReader(env.inputArgs[0], idx);
    });

    applyItems(function (fileList, map) {
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

    reduce(6, function (key,  values) {
        emit(key, D(values).size());
    });

    finish(function (results) {
        doWith(
            orzo.fileWriter('results.txt'),
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