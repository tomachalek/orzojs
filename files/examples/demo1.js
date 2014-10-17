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

//
// Sample script which demonstrates simple map-reduce operation where
// each worker generates random number, converts it to a string and
// applies md5 function. Finally it emits first character of resulting
// string. Reduce operation just collects total occurrences of each character.
//
// When performed on Intel Core i7 2600 (4 core CPU):
// 1 chunk with 800,000 generated items took about 3.3 sec. to process
// 4 chunks each with 200,000 generated items took about 1.9 sec. to process (about 74% faster)

var x = require('libs/samplelib');

orzo.dump(x.foo());

dataChunks(6, function (idx) {
    var ans = [],
        i,
        s;
    for (i  = 0; i < 10000; i += 1) {
        s = orzo.md5(String(Math.random()));
        ans.push(s);
    }
    // we return an iterator so we do not need
    // to define a custom applyItems() function
    return iterator(ans);

});

map(function (data) {
    emit(data.slice(0, 1), 1);
});

function sum(items) {
    var i,
        ans = 0;

    for (i = 0; i < items.length; i += 1) {
        ans += items[i];
    }
    return ans;
}

reduce(6, function (key,  values) {
    emit(key, D(values).sum());
});


finish(function (results) {
    var t= orzo.measureTime(function () {
        results.each(function (k, v) {
            orzo.printf('%s => %s\n', k, v[0]);
        });
    });
    orzo.print('time: ' + t);
});
