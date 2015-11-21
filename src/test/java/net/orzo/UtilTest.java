/*
 * Copyright (C) 2015 Tomas Machalek
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
package net.orzo;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author Tomas Machalek<tomas.machalek@gmail.com>
 *
 */
public class UtilTest {

    private char decimalSepar;

    @BeforeSuite
    public void beforeSuite() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        this.decimalSepar = symbols.getDecimalSeparator();
    }

    @Test
    public void testNormalizeAbsolutePath() {
        String[] pathElms = {"", "foo", "bar", "baz"};
        String testPath = Joiner.on(File.separator).join(pathElms);
        Assert.assertEquals(Util.normalizePath(testPath), "/foo/bar/baz");
    }

    @Test
    public void testNormalizeRelativePath() {
        String[] pathElms = {"foo", "bar"};
        String testPath = Joiner.on(File.separator).join(pathElms);
        Assert.assertEquals(Util.normalizePath(testPath), "foo/bar");
    }

    @Test
    public void testNormalizeRootPath() {
        String testPath = File.separator;
        Assert.assertEquals(Util.normalizePath(testPath), "/");
    }

    @Test
    public void testTimeConversion() {
        long millis = 3753971;
        String ans = Util.milliSecondsToHMS(millis);
        Assert.assertEquals(ans, String.format("01:02:33%s97", this.decimalSepar));
    }

    @Test
    public void testZeroTimeConversion() {
        long millis = 0;
        String ans = Util.milliSecondsToHMS(millis);
        Assert.assertEquals(ans, String.format("00:00:00%s00", this.decimalSepar));
    }

    @Test
    public void testNegativeTimeConversion() {
        long millis = -1;
        String ans = Util.milliSecondsToHMS(millis);
        Assert.assertEquals(ans, "-");
    }
}

