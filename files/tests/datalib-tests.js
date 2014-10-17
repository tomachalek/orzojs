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

/**
* @param {{}} scope
* @param {function(*)} D
*/
(function (scope) {
    'use strict';

    var D = scope.D;


    module("datalib.stat");

    test("each()", function () {
        var data = D(['a', 'b', 'c']),
            ans = [],
            idx = [],
            thisValues = [];

        data.each(function (v, i) {
            ans.push(v);
            idx.push(i);
            thisValues.push(this);
        });

        deepEqual(ans, ['a', 'b', 'c'], "test value parameter");
        deepEqual(idx, [0, 1, 2], "test index parameter");
        deepEqual(thisValues, ['a', 'b', 'c'], "test 'this' value");
    });

    test("each(), return false breaks iteration", function () {
        var data = D([0, 1, 2, 3, 4]),
            ans = [];

        data.each(function (v, i) {
            ans.push(this);
            if (i >= 2) {
                return false;
            }
        });
        deepEqual(ans, [0, 1, 2]);
    });

    test("sum()", function () {
        var items;

        items = [1, 2, 3, 4.2, 5, 6.1, 7, 8, 9, 10.7];
        equal(D(items).sum(), 56, "general valid numbers");

        items = [];
        equal(D(items).sum(), 0, "empty list");

        items = ['a', 1, 2, NaN];
        ok(isNaN(D(items).sum()), "list with non numbers and NaN");
    });


    test("sum() with accessor", function () {
        var items,
            customAccess;

        customAccess = function (x) {
            return x[1];
        };

        items = [['a', 1], ['b', 2], ['c', 3], ['d', 4], ['e', 5]];
        equal(D(items, customAccess).sum(), 15, 'access to structured items with valid respective items');

        items = [['a', 1], ['b', 2], ['c', 'foo'], ['d', 1], ['e', 5]];
        ok(isNaN(D(items, customAccess).sum()), 'access to structured items with some invalid items');

        items = [['a', 1], ['b', 2], ['c', NaN], ['d', 1], ['e', 5]];
        ok(isNaN(D(items, customAccess).sum()), 'access to structured items with NaN item');
    });

    test("max()", function () {
        var items = [0.4, 1, 2.7, -10, 19, 91.9];

        equal(D(items).max(), 91.9);
    });

    test("max() on empty data", function () {
        equal(isNaN(D([]).max()), true);
    });

    test("max() with invalid data", function () {
        var items = [0.4, 1, 2.7, null, 19, 91.9];

        equal(isNaN(D(items).max()), true);
    });


    test("min()", function () {
        var items = [0.4, 1, 2.7, -10, 19, 91];

        equal(D(items).min(), -10);
    });

    test("min() on empty data", function () {
        equal(isNaN(D([]).min()), true);
    });

    test("min() with invalid data", function () {
        var items = [0, 1, 2, null, 19, 91];

        equal(isNaN(D(items).min()), true);
    });


    test("average()", function () {
        var items,
            outProc;

        outProc = function (x) {
            return x.toPrecision(3);
        };

        items = [1.1, 2, 3, 4.8, 5, 6, 7, 8, 9.8, 10.1];
        equal(D(items).average().toPrecision(3), 5.68, "general valid numbers");

        items = [1.1, 5, NaN, 7];
        ok(isNaN(D(items).average()), "list with NaN item");

        items = [1.1, 5, "it's me", 7];
        ok(isNaN(D(items).average()), "list with incorrect item");

        items = [];
        ok(isNaN(D(items).average()), "empty list should have average NaN");
    });


    test("average() with accessor", function () {
        var items,
            customAccess;

        customAccess = function (x) {
            return x[1];
        };

        items = [['a', 1.1], ['b', 2], ['c', 3], ['d', 4.8], ['e', 5.19]];
        equal(D(items, customAccess).average().toPrecision(4),
            3.218, "general valid numbers");

        items = [['a', 1.1], ['b', 5], ['c', NaN], ['d', 7]];
        ok(isNaN(D(items, customAccess).average()),
            "list with NaN item");

        items = [['a', 1.1], ['b', 5], ['c', "it's me"], ['d', 7]];
        ok(isNaN(D(items, customAccess).average()),
            "list with incorrect item");
    });


    test("stdev()", function () {
        var items;

        items = [1, 2, 1, 2, 1, 2];
        equal(D(items).stdev().toPrecision(4),
            0.5477, "general valid numbers");

        items = [1.1, 5, NaN, 7];
        ok(isNaN(D(items).stdev()),
            "list with NaN item");

        items = [1.1, 5, "it's me", 7];
        ok(isNaN(D(items).stdev()),
            "list with incorrect item");

        items = [];
        ok(isNaN(D(items).stdev()), "empty list should have average NaN");
    });


    test("stdev() with accessors", function () {
        var items,
            customAccess;

        customAccess = function (x) {
            return x[1];
        };

        items = [['a', 1], ['b', 2], ['c', 1], ['d', 2], ['e', 1], ['f', 2]];
        equal(D(items, customAccess).stdev().toPrecision(4),
            0.5477, "general valid numbers");

        items = [['a', 1.1], ['b', 5], ['c', NaN], ['d', 7]];
        ok(isNaN(D(items, customAccess).stdev()),
            "list with NaN item");

        items = [['a', 1.1], ['b', 5], ['c', "it's me"], ['d', 7]];
        ok(isNaN(D(items, customAccess).stdev()),
            "list with incorrect item");

        items = [];
        ok(isNaN(D(items, customAccess).stdev()),
            "empty list should have average NaN");
    });


    test("values() with argument of the 'object' type", function () {
        var obj = {'a' : 0, 'b' : -1, 'c' : 'foo', 'd' : null, 'e' : 127},
            values = scope.values(obj);
        ok(values.indexOf(0) > -1);
        ok(values.indexOf(-1) > -1);
        ok(values.indexOf('foo') > -1);
        ok(values.indexOf(null) > -1);
        ok(values.indexOf(127) > -1);
        equal(values.length, 5);
    });

    test("values() with defined callback converting null/undefined to zero", function () {
        var obj = {'a' : 1, 'b' : 2, 'c' : undefined, 'd' : null},
            filter,
            values,
            zero1Pos,
            zero2Pos;

        filter = function (v) {
            if (v === undefined || v === null) {
                return 0;
            }
            return v;
        };

        values = scope.values(obj, filter);

        ok(values.indexOf(1) > -1);
        ok(values.indexOf(2) > -1);
        zero1Pos = values.indexOf(0);
        zero2Pos = values.indexOf(0, zero1Pos + 1);
        ok(zero1Pos > 0);
        ok(zero2Pos > 0);
        ok(zero1Pos !== zero2Pos);
        equal(values.length, 4);
    });

    test("values() with argument of a non-object type", function () {
        throws(function () {
            scope.values('foo');
        }, TypeError);
    });

    test("values() with argument equal to null", function () {
        throws(function () {
            scope.values(null);
        }, TypeError);
    });

    test("correl() of two equal vectors", function () {
        var values1 = [1, 3, 7, 8, 12, 3, 0, 4, 8, 10],
            values2 = [1, 3, 7, 8, 12, 3, 0, 4, 8, 10];

        ok(D(values1).correl(D(values2)) === 1);
    });

    test("correl() positive correlation coefficient 1", function () {
        var values1 = [1, 2, 3, 4, 5, 6],
            values2 = [1, 2, 3, 4, 5, 6],
            ans;

        ans = D(values1).correl(D(values2));
        ok(ans.toFixed(2) === '1.00');
    });

    test("correl() negative correlation coefficient -1", function () {
        var values1 = [1, 2, 3, 4, 5, 6],
            values2 = [6, 5, 4, 3, 2, 1],
            ans;

        ans = D(values1).correl(D(values2));
        ok(ans.toFixed(2) === '-1.00');
    });

    test("correl() with correct arguments", function () {
        var values1 = [1, 2, 3, 4, 5, 6],
            values2 = [4, 7, 3, 4, 1, 2],
            ans;

        ans = D(values1).correl(D(values2));
        ok(ans.toFixed(6) === '-0.695978');
    });

    test("correl() with empty arguments", function () {
        var values1 = [],
            values2 = [],
            ans;

        ans = D(values1).correl(D(values2));
        ok(isNaN(ans));
    });

    test("correl() with non-numeric arguments", function () {
        var values1 = ["foo", "bar"],
            values2 = ["x", "y"],
            ans;

        ans = D(values1).correl(D(values2));
        ok(isNaN(ans));
    });

    test("correl() with args of different lengths", function () {
        var values1 = [0, 1, 2, 3, 4, 5, 6],
            values2 = [0, 1, 2],
            ans;

        ans = D(values1).correl(D(values2));
        ok(isNaN(ans));
    });

    test("orzo's original uniq function is overridden by datalib one", function () {
        var d1 = D(['foo', 'bar', 'bar', 'foo', 'foo', 'hi']),
            d2,
            tst = [];

        d2 = orzo.uniq(d1);

        equal(d2.toString(), '[object Data]');
        equal(d2.size(), 3);
        d2.each(function (item) {
            tst.push(item);
        });
        tst = tst.sort();
        deepEqual(tst, ['bar', 'foo', 'hi']);
    });


}(this));