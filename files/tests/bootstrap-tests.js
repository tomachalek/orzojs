/*
 * Copyright (C) 2014 Tomas Machalek
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

(function (scope) {
    module("bootstrap");

    test("iterator() - non empty array", function () {
        var x = [0, 1, 2, 3, 4, "last"],
            iter = iterator(x),
            ans = [];

        while (iter.hasNext()) {
            ans.push(iter.next());
        }
        deepEqual(ans, x);
    });

    test("iterator() - empty array", function () {
        var x = [],
            iter = iterator(x),
            ans = [];

        while (iter.hasNext()) {
            ans.push(iter.next());
        }
        deepEqual(ans, x);
    });

    test("iterator() - invalid data", function () {
       var x = {};

       throws(function () { iterator(x) }, TypeError, 'Throws TypeError in case of invalid data');
    });

    test("iterator() - non array with custom next(), hasNext()", function () {
        var x = {
                data : ['a', 'b', 'c', 'd', 'e']
            },
            next = function () {
                return x.data.pop();
            },
            hasNext = function () {
                return x.data.length > 0;
            },
            iter,
            ans = [];


        iter = iterator(x, next, hasNext);
        while (iter.hasNext()) {
            ans.push(iter.next());
        }
        deepEqual(ans, ['a', 'b', 'c', 'd', 'e'].reverse());
    });

    test("iterator() - operates on original data (i.e. no array copies etc.)", function () {
        var x = [0, 1, 2, 3],
            iter1 = iterator(x),
            iter2 = iterator(x);

        strictEqual(iter1.data, iter2.data);
    });

    test("doWith() with a closeable object - test the 'close' has been called and the passed argument works",
        function () {
            var status = false,
                obj = {
                    closed: false,
                    close: function () {
                        status = true;
                    },
                    readData: function () {
                        return "a value";
                    }
                };

            scope.doWith(obj, function (o) {
                equal(o.readData(), "a value");
            });
            equal(status, true);
        });


    test("doWith() with a closeable object, test 'this' reference is working", function () {
        var obj = {
            value : 0
        };

        scope.doWith(obj, function () {
            this.value = 1;
        });

        equal(obj.value, 1);
    });

}(this));