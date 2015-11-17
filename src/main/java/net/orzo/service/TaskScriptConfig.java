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
package net.orzo.service;

import java.io.File;
import java.io.IOException;

import net.orzo.scripting.ScriptConfigurationException;
import net.orzo.scripting.SourceCode;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TaskScriptConfig implements ScriptConfig {

    private final String scriptPath;

    private final String libraryPath;

    private final String description;

    private final String[] defaultArgs;

    public TaskScriptConfig(String scriptPath, String libraryPath, String description,
                            String[] defaultArgs) {
        super();
        this.scriptPath = scriptPath;
        this.libraryPath = libraryPath;
        this.description = description;
        this.defaultArgs = defaultArgs;
    }

    /**
     * @throws ScriptConfigurationException
     */
    @Override
    public SourceCode getScript() {
        try {
            return SourceCode.fromFile(new File(scriptPath));
        } catch (IOException e) {
            throw new ScriptConfigurationException("Failed to load script "
                    + scriptPath, e);
        }
    }

    @Override
    public String getScriptPath() {
        return this.scriptPath;
    }

    @Override
    public String getLibraryPath() {
        return this.libraryPath;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("ScriptConfig {scriptPath: %s, libraryPath: %s}",
                this.scriptPath, this.libraryPath);
    }

    @Override
    public String[] getDefaultArgs() {
        return this.defaultArgs != null ? this.defaultArgs : new String[0];
    }

}
