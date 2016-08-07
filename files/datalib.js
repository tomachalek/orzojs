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
 * This file contains orzo's library of useful functions
 */
(function (scope) {
    'use strict';

    /**
     *
     * @param data
     * @param accessFn
     * @constructor
     */
    function Data(data, accessFn) {
        this.data = data;
        if (typeof accessFn === 'function') {
            this.accessorFunc = accessFn;

        } else {
            this.accessorFunc = function (x) { return x; }
        }
    }

    /**
     *
     * @returns {string}
     */
    Data.prototype.toString = function () {
        return '[object Data]';
    };

    /**
     *
     */
    Data.prototype.size = function () {
        return this.data.length;
    };

    /**
     *
     * @param idx
     * @returns {*}
     */
    Data.prototype.get = function (idx) {
        return this.accessorFunc(this.data[idx]);
    };

    /**
     *
     */
    Data.prototype.set = function (idx, v) {
        this.data[idx] = v;
    };

    /**
     * Iterates over data and applies passed function.
     * To break the iteration function must return false.
     *
     * @param {function} fn a function with signature function (value, index)
     */
    Data.prototype.each = function (fn) {
        var i,
            ans;

        for (i = 0; i < this.data.length; i += 1) {
            ans = fn.call(this.data[i], this.data[i], i);
            if (ans === false) {
                break;
            }
        }
    };

    /**
     * Calculates sum of provided numbers. If a non-number is encountered NaN is returned.
     *
     * @returns {*}
     */
    Data.prototype.sum = function () {
        var total = 0,
            i,
            x;

        for (i = 0; i < this.size(); i += 1) {
            x = this.get(i);
            if (typeof x !== 'number') {
                return NaN;
            }
            total += x;
        }
        return total;
    };

    /**
     * Finds maximal element in the data. If there is
     * even a single non-numerical element then NaN is returned.
     *
     * @returns {number}
     */
    Data.prototype.max  = function () {
        var maxVal = this.get(0),
            x,
            i;

        for (i = 1; i < this.size(); i += 1) {
            x = this.get(i);
            if (typeof x !== 'number') {
                return NaN;

            } else if (x > maxVal) {
                maxVal = x;
            }
        }
        return maxVal;
    };

    /**
     * Finds maximal element in the data. If there is
     * even a single non-numerical element then NaN is returned.
     *
     * @returns {number}
     */
    Data.prototype.min  = function () {
        var minVal = this.get(0),
            x,
            i;

        for (i = 1; i < this.size(); i += 1) {
            x = this.get(i);
            if (typeof x !== 'number') {
                return NaN;

            } else if (x < minVal) {
                minVal = x;
            }
        }
        return minVal;
    };

    /**
     * Calculates arithmetic average of provided numbers
     *
     */
    Data.prototype.average = function () {
        if (this.size() > 0) {
            return this.sum() / this.size();
        }
        return NaN;
    };

    /**
     * Calculates standard deviation of the sample
     *
     * @returns {*} standard deviation of the sample or NaN in case
     * the value cannot be calculated
     */
    Data.prototype.stdev = function () {
        var mean,
            curr = 0,
            i;

        mean = this.average();
        if (!isNaN(mean) && this.size() > 1) {
            for (i = 0; i < this.size(); i += 1) {
                curr += (this.get(i) - mean) * (this.get(i) - mean);
            }
            return Math.sqrt(curr / (this.size() - 1));
        }
        return NaN;
    };

    /**
     * Calculates Pearson product-moment correlation coefficient
     * between this data and other data.
     * (http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient)
     *
     * @param otherData
     * @returns {number}
     */
    Data.prototype.correl = function (otherData) {
        var i,
            len = Math.min(this.size(), otherData.size()),
            m1,
            m2,
            numerator = 0,
            denominator1 = 0,
            denominator2 = 0;

        if (this.size() === otherData.size()) {
            m1 = this.average();
            m2 = otherData.average();
            for (i = 0; i < len; i += 1) {
                numerator += (this.get(i) - m1) * (otherData.get(i) - m2);
                denominator1 += (this.get(i) - m1) * (this.get(i) - m1);
                denominator2 += (otherData.get(i) - m2) * (otherData.get(i) - m2);
            }
            return numerator / Math.sqrt((denominator1 * denominator2));

        } else {
            return Math.NaN;
        }
    };

    /**
     * Calculates a median of the dataset. This function
     * alters the order of the data (but does not sort them)
     * to prevent exhausting RAM.
     */
    Data.prototype.median = function () {
        var self = this;

        if (this.size() === 0) {
            return NaN;
        }

        function swap(i, j) {
            var tmp = self.data[i];
            self.data[i] = self.data[j];
            self.data[j] = tmp;
        }

        function partition(left, right, pivotIdx) {
            var pivotValue = self.get(pivotIdx);
            var realPivotIdx = left;
            var i;

            swap(pivotIdx, right);
            for (i = left; i <= right; i += 1) {
                if (self.get(i) < pivotValue) {
                    swap(i, realPivotIdx);
                    realPivotIdx += 1;
                }
            }
            swap(right, realPivotIdx);
            return realPivotIdx;
        }

        function quickSelect(n) {
            var left = 0;
            var right = self.size() - 1;
            var pivotIdx;

            while (true) {
                if (left == right) {
                    return self.get(left);
                }
                pivotIdx = Math.floor((left + right) / 2);
                pivotIdx = partition(left, right, pivotIdx);
                if (n == pivotIdx) {
                    return self.get(n);

                } else if (n < pivotIdx) {
                    right = pivotIdx - 1;

                } else {
                    left = pivotIdx + 1;
                }
            }
        }
        var halfIdx = Math.floor(self.size() / 2);
        var m = quickSelect(halfIdx);
        var m2;
        if (self.size() % 2 == 0) {
            m2 = quickSelect(halfIdx - 1);
            return (m2 + m) / 2;

        } else {
            return m;
        }
    }


    /**
     * Just a convenient way how to create Data object
     * @param {Array} d
     * @param {Function} [accessFx]
     * @returns {Data}
     */
    scope.D = function (d, accessFx) {
        return new Data(d, accessFx);
    };


    /**
     * Returns values of an object (i.e. the values of all object's own properties).
     * Optionally, a transform function can be provided to change specific values into
     * other ones (e.g. undefined to zero).
     *
     * @param {object} obj
     * @param {function(value) -> *} [transform] optional function to transform values
     * @return {array} list of values
     * @throws {Error} If the obj argument is null or of a non-object type
     */
    scope.values = function (obj, transform) {
        var ans = [],
            k;

        transform = transform || function (v) {
            return v;
        };

        if (typeof obj !== 'object' || obj === null) {
            throw new TypeError('Agrument must be a not-null object');
        }
        for (k in obj) {
            if (obj.hasOwnProperty(k)) {
                ans.push(transform(obj[k]));
            }
        }
        return ans;
    };

    /**
     *
     */
    function createUniqFn(origFn) {
        var origUniq = origFn;

        return function (data, keyFn) {
            var ans,
                newKeyFn;

            // we test Array type first to avoid dumping long arrays
            if (Object.prototype.toString.call(data) !== '[object Array]'
                && data.toString() === '[object Data]') {
                newKeyFn = keyFn || data.accessorFunc;
                ans = origUniq(data.data, newKeyFn);
                return new Data(ans, newKeyFn);

            } else {
                ans = origUniq(data, keyFn);
            }
            return ans;
        };
    }

    // datalib modifies orzo's original uniq() function to support Data object.

    if (scope.hasOwnProperty('orzo') && scope.orzo.uniq !== scope.uniq) {
        scope.orzo.uniq = createUniqFn(scope.orzo.uniq);
    }

}(this));

