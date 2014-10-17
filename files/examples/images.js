// /////////////////////////////////////////////////
// A minimalist working template of a Orzo.js script.
// Additional command line arguments are accessible
// in env.inputArgs array.
// /////////////////////////////////////////////////

(function () {
    'use strict';

    function areaHistogram(image, coords, size) {
        var maxX = image.width - size.x,
            maxY = image.height - size.y;

        return image.areaHistogram(coords.x * maxX, coords.y * maxY, size.x, size.y);
    }

    function randomCoords() {
        return {
            x : Math.random(),
            y : Math.random()
        }
    }

    function tileSize(image, scale) {
        return {
            x : image.width * scale,
            y : image.height * scale
        }
    }

    /*
     * Here we specify how data chunks are defined and how
     * many chunks are there (4)
     */
    dataChunks(4, function (idx) {
        return orzo.filePairGenerator('D:/Pictures/syntheum', idx);
    });

    applyItems(function (dataChunk, map) {
        var files;

        while(dataChunk.hasNext()) {
            files = dataChunk.next();
            map({
                file1 : files[0],
                file2 : files[1]
            });
        }
    });

    /*
     * Now we specify the relationship between a single chunk of data
     * and the map() function.
     */
    /*
    applyItems(function (dataChunk, map) {
        var file1, file2, image1, image2, vector1, vector2, tile1, tile2;

        if (dataChunk.hasNext()) {
            file1 = dataChunk.next();
        }
        if (dataChunk.hasNext()) {
            file2 = dataChunk.next();
        }

        if (file1 && file2) {
            orzo.printf('file1 = %s\n', file1);
            orzo.printf('file2 = %s\n', file2);

            image1 = orzo.loadImage(file1);
            image2 = orzo.loadImage(file2);

            tile1 = tileSize(image1, 0.1);
            tile2 = tileSize(image2, 0.1);

            (function () {
                var i, sum = [], coords;

                for (i = 0; i < 100; i += 1) {
                    coords = randomCoords();
                    vector1 = D(randomTile(image1, coords, tile1));
                    vector2 = D(randomTile(image2, coords, tile2));
                    sum.push(vector1.correl(vector2));
                }
                orzo.printf('avg correl: %01.2f\n', D(sum).average());
            }());
        }
    });
    */

    /*
     * Here, the map() function is defined. It is expected to
     * emit a key and a value.
     */
    map(function (data) {
        var image1, image2, tile1, tile2, vector1, vector2;


        orzo.printf('file1 = %s\n', data.file1);
        orzo.printf('file2 = %s\n', data.file2);

        image1 = orzo.loadImage(data.file1);
        image2 = orzo.loadImage(data.file2);
        //orzo.dump(image1.width);

        if (image1 != null && image2 != null) {
            tile1 = tileSize(image1, 0.1);
            tile2 = tileSize(image2, 0.1);
            sum = [];

            (function () {
                var i, coords;

                for (i = 0; i < 20; i += 1) {
                    coords = randomCoords();
                    vector1 = D(areaHistogram(image1, coords, tile1));
                    vector2 = D(areaHistogram(image2, coords, tile2));
                    sum.push(vector1.correl(vector2));
                }
                orzo.printf('avg avg: %01.2f, size = %01.2f\n', D(sum).average(), D(sum).size());
            }());
            emit(data.file1, { file: data.file2, similarity:  D(sum).average()});
            emit(data.file2, { file: data.file1, similarity:  D(sum).average()});

        } else {
            emit(data.file1, { file: data.file2, similarity:  0});
            emit(data.file2, { file: data.file1, similarity:  0});
        }
    });

    /*
     * Here we define the reduce() function. It is expected
     * to emit a single value (of any, even a complex type).
     * Optionally, we can define number of parallel workers
     * (6 here).
     */
    reduce(6, function (key,  values) {
        emit(key, values);
    });

    /*
     * Finish function is run once all the data is processed.
     * Values emitted by the reduce() function are available as
     * an Array.
     */
    finish(function (results, info) {
        doWith(orzo.fileWriter('pictures.txt'), function (fw) {
            results.each(function (key, values) {
                var sorted;
                fw.writeln(orzo.sprintf('%s -------------->', key));
                sorted = values[0].sort(function (a, b) {
                    if (a.similarity < b.similarity) {
                        return 1;
                    }
                    return -1;
                });
                sorted.forEach(function (item, i) {
                    fw.writeln(orzo.sprintf('\t%s (%01.2f)', item.file, item.similarity));
                });
                fw.writeln('\n');
            });
            orzo.dump(info);
        });
    });

}());