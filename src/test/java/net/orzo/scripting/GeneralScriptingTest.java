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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.orzo.tools.ResourceLoader;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class GeneralScriptingTest {

	/**
	 * 
	 */
	private ScriptContext context;

	/**
	 * 
	 */
	private Bindings scope;

	/**
	 * 
	 * @param script
	 * @return
	 * @throws ScriptException 
	 */
	public Object runScript(String script) throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		this.context = engine.getContext();
		this.scope = this.context.getBindings(ScriptContext.GLOBAL_SCOPE); // TODO do we need this?
		Compilable compilable = (Compilable)engine;
		return compilable.compile(script);
	}

	/**
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 * @throws ScriptException 
	 */
	public Object runScriptResource(String resource) throws IOException, ScriptException {
		return runScript(new ResourceLoader().getResourceAsString(resource));
	}

	/**
	 * 
	 * @return
	 */
	public Bindings getScope() {
		return this.scope;
	}

	/**
	 * 
	 */
	public ScriptContext getContext() {
		return this.context;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Object getValue(String id) {
		return this.scope.get(id);
	}

}
