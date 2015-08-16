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

import java.io.IOException;

import net.orzo.scripting.SourceCode;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class InternalScriptConfig implements ScriptConfig {

	private final SourceCode script;

	private final String libraryPath;

	private final String workingDirPath;

	/**
	 * 
	 * @param script
	 * @param libraryPath
	 */
	public InternalScriptConfig(SourceCode script, String libraryPath,
			String workingDirPath) {
		this.script = script;
		this.libraryPath = libraryPath;
		this.workingDirPath = workingDirPath;
	}

	/**
	 * 
	 */
	@Override
	public SourceCode getScript() throws IOException {
		return this.script;
	}

	@Override
	public String getScriptPath() {
		return this.script.getFullyQualifiedName();
	}

	@Override
	public String getLibraryPath() {
		return this.libraryPath;
	}

	@Override
	public String getWorkingDirPath() {
		return this.workingDirPath;
	}

}
