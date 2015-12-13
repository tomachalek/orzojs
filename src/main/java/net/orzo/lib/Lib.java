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
package net.orzo.lib;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.MySQLConnection;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptFunction;
import net.orzo.data.Database;
import net.orzo.data.MySqlDb;
import net.orzo.data.Web;
import net.orzo.data.graphics.GreyscalePicture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miscellaneous utilities and backend-related functionality for the JavaScript
 * environment. Please note that this object is not state-less. Some operations
 * create persistent objects which are reused. This diminishes need for global
 * objects within user scripts (e.g. you can create the same chunked file reader
 * multiple times in your dataChunks() function and it is still the same
 * object).
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@SuppressWarnings("restriction")
public class Lib {

    public final Files files;

    public final Web web;

    public final Strings strings;

    public final DataStructures dataStructures;

    public final RestClient restClient;

    @SuppressWarnings(value = {"unused"})
    private static final Logger LOG = LoggerFactory.getLogger("user_script");

    /**
     *
     */
    public Lib() {
        this.files = new Files();
        this.web = new Web();
        this.strings = new Strings();
        this.dataStructures = new DataStructures();
        this.restClient = new RestClient();
    }

    /**
     * @param t time in seconds (fractions are available, e.g. sleep(3.7))
     * @throws InterruptedException
     */
    public void sleep(double t) throws InterruptedException {
        Thread.sleep(Math.round(t * 1000));
    }

    /**
     * Returns a number of available processors. Please note that Intel CPUs
     * with hyper-threading report twice as high as is actual number of physical
     * cores.
     */
    public int numOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Measures the execution time of the provided function. Please note that in
     * case of asynchronous code you may not obtain the value you have been
     * expecting.
     *
     * @param function
     * @return time in milliseconds
     */
    public long measureTime(ScriptFunction function) {
        long startTime = System.currentTimeMillis();
        MethodHandle mh = function.getBoundInvokeHandle(Context.getContext());

        try {
            mh.invoke();

        } catch (Throwable e) {
            throw new LibException(e);
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * @param coll
     * @param cmp
     * @throws Throwable
     */
    public void sortList(List<Object> coll, ScriptFunction cmp) {
        ErrorHandlingComparator javaCmp = new ErrorHandlingComparator(cmp);
        Collections.sort(coll, javaCmp);
        if (javaCmp.lastError() != null) {
            throw new RuntimeException(String.format(
                    "Failed to sort the list: %s", javaCmp.lastError()
                            .getMessage()), javaCmp.lastError());
        }
    }

    /**
     */
    public GreyscalePicture loadImage(String path) throws IOException {
        return GreyscalePicture.load(path);
    }


    public Database connectToDb(String type, String uri) throws SQLException {
        if (type.equals("mysql")) {
            return new MySqlDb().connect(uri);

        } else {
            throw new IllegalArgumentException("Unknown db type " + type);
        }
    }

}
