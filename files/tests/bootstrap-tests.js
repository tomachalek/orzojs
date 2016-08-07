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
            equal(this, scope);
        });
    });

    test("doWith() with multiple objects", function () {
        var res1, res2, res3;

        function Resource(id) {
            this.id = id;
            this.touched = false;
            this.closed = false;
            this.close = function () {
                this.closed = true;
            }
        }

        res1 = new Resource(1);
        res2 = new Resource(2);
        res3 = new Resource(3);

        doWith([res1, res2, res3], function (r1, r2, r3) {
            r1.touched = true;
            r2.touched = true;
            r3.touched = true;
        });

        equal(res1.touched, true);
        equal(res2.touched, true);
        equal(res3.touched, true);

        equal(res1.closed, true);
        equal(res2.closed, true);
        equal(res3.closed, true);
    });


    test("doWith() with multiple objects - assure closing order", function () {
        var res1, res2, res3, marks = [];

        function Resource(id) {
            this.id = id;
            this.close = function () {
                marks.push(this.id);
            }
        }

        res1 = new Resource(1);
        res2 = new Resource(2);
        res3 = new Resource(3);

        doWith([res1, res2, res3], function (r1, r2, r3) {
        });

        deepEqual(marks, [3, 2, 1]);
    });


    test("doWith() with multiple objects - assure closing order on error", function () {
        var res1, res2, res3, marks = [];

        function Resource(id) {
            this.id = id;
            this.close = function () {
                marks.push(this.id);
            }
        }

        res1 = new Resource(1);
        res2 = new Resource(2);
        res3 = new Resource(3);

        doWith([res1, res2, res3], function () {
            throw new Error('failed');
        });

        deepEqual(marks, [3, 2, 1]);
    });

}(this));