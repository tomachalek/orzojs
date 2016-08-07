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
package net.orzo;

import net.orzo.scripting.SourceCode;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class CalculationParams {

    public SourceCode userenvScript;

    public SourceCode datalibScript;

    public SourceCode userScript;

    public SourceCode calculationScript;

    /**
     * A directory where user script is located.
     */
    public String workingDirModulesPath;

    /**
     * An additional user-defined libs location
     */
    public String optionalModulesPath;

    /**
     * Command line parameters for the script
     */
    public String[] inputValues;

}
