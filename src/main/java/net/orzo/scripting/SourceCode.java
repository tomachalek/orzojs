/*
 * Copyright (c) 2013 Tomas Machalek
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

package net.orzo.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import net.orzo.tools.ResourceLoader;

import org.apache.commons.io.input.BOMInputStream;

import com.google.common.io.ByteStreams;


/**
 * Represents general source code with some identifier/name and contents. The
 * class itself does not provide any methods to determine whether the contents
 * is a "script", even a text (i.e. it is possible to initialize an instance
 * with binary data too and no error occurs until it is used somewhere where a
 * text is expected).
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class SourceCode {

    /**
     *
     */
    private final String fullyQualifiedName;

    /**
     *
     */
    private final String name;

    /**
     *
     */
    private final String contents;

    /**
     * @param fullyQualifiedName full identification of the script (~ path)
     * @param name               name of the script (~ filename)
     * @param contents           source code of the script
     */
    public SourceCode(String fullyQualifiedName, String name, String contents) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.name = name != null ? name : "unnamed";
        this.contents = contents != null ? contents : "";
    }

    /**
     *
     */
    @Override
    public String toString() {
        return String.format("<%s> (length: %s)", this.fullyQualifiedName,
                this.contents.length());
    }

    /**
     * Returns informative name of the script (it can be e.g. a file name)
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns full identification of the script (e.g. resource path, file path)
     *
     */
    public String getFullyQualifiedName() {
        return this.fullyQualifiedName;
    }

    /**
     */
    public String getContents() {
        return contents;
    }

    /**
     * @throws ScriptException
     */
    public CompiledScript compile(Compilable compilable) throws ScriptException {
        return compilable.compile(getContents());
    }

    /**
     * Creates a source code object using contents of provided file. UTF-8 BOM
     * (if present) is automatically removed.
     *
     * @param f text file containing source code
     * @return source code containing contents of file f
     */
    public static SourceCode fromFile(File f) throws IOException {
        try (BOMInputStream bis = new BOMInputStream(new FileInputStream(f))) {
            return new SourceCode(f.getAbsolutePath(), f.getName(), new String(
                    ByteStreams.toByteArray(bis), "UTF-8"));
        }
    }

    /**
     * Creates source code from a Java resource identified by its absolute path
     * (e.g. net/orzo/userenv.js)
     *
     * @param res
     */
    public static SourceCode fromResource(String res) throws IOException {
        String id = res.substring(Math.max(0, res.lastIndexOf("/") + 1));
        InputStream sourceStream = new ResourceLoader().getResourceStream(res);
        if (sourceStream == null) {
            throw new IOException("Empty resource: " + res);
        }
        try (BOMInputStream bis = new BOMInputStream(sourceStream)) {
            String source = new String(ByteStreams.toByteArray(bis));
            return new SourceCode(res, id, source);
        }
    }
}
