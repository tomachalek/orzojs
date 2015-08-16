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

import net.orzo.scripting.SourceCode;
import net.orzo.service.InternalScriptConfig;
import net.orzo.service.ScriptConfig;
import net.orzo.service.ServiceConfig;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class CmdConfig implements ServiceConfig {

	private final String scriptId;

	private final InternalScriptConfig scriptConfig;

	public CmdConfig(String scriptId, SourceCode userScript, String libPath,
			String workingDirPath) {
		this.scriptId = scriptId;
		this.scriptConfig = new InternalScriptConfig(userScript, libPath,
				workingDirPath);
	}

	@Override
	public boolean isAllowedScript(String id) {
		return this.scriptId == id;
	}

	@Override
	public ScriptConfig getScriptPath(String id) {
		if (isAllowedScript(id)) {
			return this.scriptConfig;
		}
		return null;
	}

}
