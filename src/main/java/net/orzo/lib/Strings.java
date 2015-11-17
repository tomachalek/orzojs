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
package net.orzo.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Strings {

    private static Logger LOG = LoggerFactory.getLogger(Strings.class);

    /**
     * Prints an object to the standard output and appends a new line.
     */
    public void print(Object o) {
        System.out.println(o);
    }

    /**
     */
    public void printf(String s, Object[] args) {
        System.out.print(sprintf(s, args));
    }

    /**
     * Works in a similar way to Java's String.format but with respect to
     * JavaScript numeric type (= you can pass a JS number to the %d flag).
     *
     * @param s
     * @param args
     * @return formatted string with flags replaced by passed values
     */
    public Object sprintf(String s, Object[] args) {
        // we must deal here with the problem that in JavaScript there is only a
        // single numeric type
        // Number which is converted to Double. If a user wants to print
        // integers, then %d flag does
        // not work.
        Pattern p = Pattern
                .compile("(?<!%)%(0[0-9]+|\\+|,|-|\\.\\d|\\d\\.\\d)?(s|d|f|n|tB|td|te|ty|tY|tl|tM|tp|tD)");
        Matcher m = p.matcher(s);
        Object[] modArgs = new Object[args.length];
        int i = 0;
        while (m.find()) {
            if (m.group().endsWith("d") && args[i] instanceof Double) {
                modArgs[i] = (int) Math.round((double) args[i]);
                if ((int) modArgs[i] != (double) args[i]) {
                    LOG.warn(String.format(
                            "Lost precision in sprintf (%s rounded to %d)",
                            args[i], modArgs[i]));
                }

            } else {
                modArgs[i] = args[i];
            }
            i++;
        }
        return String.format(s, modArgs); // TODO wrapping ???
    }

    /**
     *
     */
    public Object hash(String value, String algorithm) {
        if (value == null) {
            throw new RuntimeException(
                    "null value not accepted in hash() function");
        }
        switch (algorithm.toLowerCase()) {
            case "md5":
                return DigestUtils.md5Hex(value);
            case "sha1":
                return DigestUtils.sha1Hex(value);
            case "sha256":
                return DigestUtils.sha256Hex(value);
            case "sha384":
                return DigestUtils.sha384Hex(value);
            case "sha512":
                return DigestUtils.sha512Hex(value);
            default:
                throw new RuntimeException(String.format(
                        "Unknown hash function '%s'", algorithm));
        }
    }


    public StringDistances stringDistance = new StringDistances() {

        public Integer levenshtein(String s1, String s2) {
            return StringUtils.getLevenshteinDistance(s1, s2);
        }

        public Integer fuzzy(String s1, String s2, String locale) {
            return StringUtils.getFuzzyDistance(s1, s2, new Locale(locale));
        }

        public Double jaroWinkler(String s1, String s2) {
            return StringUtils.getJaroWinklerDistance(s1, s2);
        }

        private double lengthCompressed(String s) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            gos.write(s.getBytes());
            IOUtils.closeQuietly(gos);
            return (double) baos.toByteArray().length;
        }

        public Double normalizedCompression(String s1, String s2) {
            try {
                double l1 = lengthCompressed(s1);
                double l2 = lengthCompressed(s2);

                return (lengthCompressed(s1 + s2) - Math.min(l1, l2))
                        / Math.max(l1, l2);

            } catch (IOException e) {
                return Double.NaN;
            }

        }

    };

}
