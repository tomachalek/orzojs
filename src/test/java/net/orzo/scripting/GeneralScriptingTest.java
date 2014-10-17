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
package net.orzo.scripting;

import java.io.IOException;

import net.orzo.tools.ResourceLoader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class GeneralScriptingTest {

	/**
	 * 
	 */
	private Context context;

	/**
	 * 
	 */
	private Scriptable scope;

	/**
	 * 
	 * @param script
	 * @return
	 */
	public Object runScript(String script) {
		this.context = Context.enter();
		this.scope = new ImporterTopLevel(this.context);
		return context.evaluateString(scope, script, "<script>", 1, null);
	}

	/**
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public Object runScriptResource(String resource) throws IOException {
		return runScript(new ResourceLoader().getResourceAsString(resource));
	}

	/**
	 * 
	 * @return
	 */
	public Scriptable getScope() {
		return this.scope;
	}

	/**
	 * 
	 */
	public Context getContext() {
		return this.context;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Object getValue(String id) {
		return this.scope.get(id, this.scope);
	}

}
