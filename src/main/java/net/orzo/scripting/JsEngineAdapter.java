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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.orzo.IntermediateResults;
import net.orzo.lib.Lib;

import com.google.common.base.Joiner;

/**
 * Javascript engine wrapper to serve within the application. Please note that
 * it is not thread-safe (i.e. each worker must have its JsEngineAdapter
 * instance).
 * 
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@SuppressWarnings("restriction")
public class JsEngineAdapter {

	/**
	 * 
	 */
	private Lib system;

	/**
	 * 
	 */
	private ScriptEngine engine;

	/**
	 * 
	 */
	private ScriptContext context;

	/**
	 * Main scope. Please note that some other operations (e.g. module loader)
	 * may create additional "local" scopes.
	 */
	private Bindings scope;

	/**
	 * 
	 */
	private final EnvParams envParams;

	/**
	 * 
	 */
	private IntermediateResults intermediateResults;

	/**
	 * 
	 */
	private final Map<String, Object> modules;

	private static final String MODULE_SEPARATOR = "/";

	/**
	 * 
	 */
	public Function<String, Object> require = new Function<String, Object>() {

		@Override
		public Object apply(String moduleId) {
			try {
				File module = findModule(
						JsEngineAdapter.this.envParams.modulesPaths, moduleId);
				if (module != null) {
					return loadModule(moduleId, SourceCode.fromFile(module));

				} else {
					throw new ModuleException(String.format(
							"Module <%s> not found.", moduleId));
				}

			} catch (IOException | ScriptException e) {
				throw new ModuleException(String.format(
						"Failed to load module <%s> with error %s", moduleId,
						e.getMessage()), e);
			}
		}
	};

	/**
	 * 
	 * @param modulePaths
	 * @param moduleId
	 * @return
	 * @throws ModuleException
	 *             if you try to load non-sandboxed module
	 */
	public static File findModule(List<String> modulePaths, String moduleId) {
		if (moduleId.startsWith(String.format(".%s", File.separator))
				|| moduleId.startsWith(String.format("..%s", File.separator))
				|| moduleId.startsWith(File.separator)
				|| moduleId.startsWith(String.format(".%s", MODULE_SEPARATOR))
				|| moduleId.startsWith(String.format("..%s", MODULE_SEPARATOR))
				|| moduleId.startsWith(MODULE_SEPARATOR)) {
			throw new ModuleException(
					"Orzo.js supports only sandboxed module loading");
		}
		String[] moduleElms = moduleId.split(MODULE_SEPARATOR);
		String fsCompatibleId = Joiner.on(MODULE_SEPARATOR).join(moduleElms);

		for (String mp : modulePaths) {
			String sysDepPath = mp.replaceAll("/", File.separator);
			File f = new File(String.format("%s%s%s.js", sysDepPath,
					File.separator, fsCompatibleId));
			if (f.isFile()) {
				return f;
			}
		}
		return null;
	}

	/**
	 * @param envParams
	 * @param funcHandler
	 * @param intermediateResults
	 */
	public JsEngineAdapter(EnvParams envParams,
			IntermediateResults intermediateResults) {
		this.envParams = envParams;
		this.intermediateResults = intermediateResults;
		this.modules = new HashMap<>();
	}

	/**
	 * 
	 */
	public JsEngineAdapter(EnvParams envParams) {
		this(envParams, null);
	}

	/**
	 * 
	 */
	public void beginWork() {
		beginWork(null);
	}

	/**
	 * 
	 */
	public void beginWork(Map<String, Object> globals) {
		this.engine = new ScriptEngineManager().getEngineByName("nashorn");
		this.context = engine.getContext();
		this.scope = this.context.getBindings(ScriptContext.ENGINE_SCOPE);
		this.system = new Lib();
		if (globals != null) {
			for (String globalVar : globals.keySet()) {
				this.scope.put(globalVar, globals.get(globalVar));
			}
		}

		this.scope.put("require", this.require);

		// objects with the "_" prefix are not intended to be used directly from
		// within javascript
		this.scope.put("_lib", this.system);
		this.scope.put("_env", this.envParams);
		if (this.intermediateResults != null) {
			this.scope.put("_result", this.intermediateResults);
		}
	}

	/**
	 * 
	 */
	public void endWork() {
	}

	/**
	 */
	public Object loadModule(String moduleId, SourceCode code)
			throws ScriptException,
			IOException {
		if (!this.modules.containsKey(code.getFullyQualifiedName())) {
			ScriptContext context = new SimpleScriptContext();
			context.setBindings(this.engine.createBindings(),
					ScriptContext.ENGINE_SCOPE);

			Bindings engineScope = context
					.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScope.put("require", this.require);
			engineScope.put("doWith", this.scope.get("doWith"));
			engineScope.put("orzo", this.scope.get("orzo"));
			engineScope.put("_moduleId", moduleId);

			SourceCode modEnv = SourceCode.fromResource("net/orzo/modenv.js");
			CompiledScript moduleScript;

			// empty module
			moduleScript = modEnv.compile((Compilable) this.engine);
			moduleScript.eval(context);

			// actual module
			moduleScript = code.compile((Compilable) this.engine);
			this.engine.put(ScriptEngine.FILENAME, code.getName());
			moduleScript.eval(context);

			ScriptObjectMirror moduleObj = (ScriptObjectMirror) engineScope
					.get("module");
			this.modules.put(code.getFullyQualifiedName(),
					moduleObj.getMember("exports"));
		}
		return this.modules.get(code.getFullyQualifiedName());
	}

	/**
	 * Sets object within the scripting environment
	 * 
	 */
	public void put(String key, Object value) {
		this.scope.put(key, value);
	}

	/**
	 * Retrieves a value from the scripting environment
	 * 
	 */
	public Object get(String key) {
		return this.scope.get(key);
	}

	/**
	 * 
	 * @param name
	 * @param args
	 *            arguments of the function
	 * @return result of the called function
	 */
	public Object runFunction(String name, Object... args)
			throws NoSuchMethodException, ScriptException {
		Invocable inv = (Invocable) this.engine;
		return inv.invokeFunction(name, args);
	}

	/**
	 * Runs specified source code without any implicit imports.
	 * 
	 * @return The return value from the last script execution
	 */
	public Object runCode(SourceCode... sourceCodes) throws ScriptException {
		Object ans = null;
		if (this.engine == null || this.context == null || this.scope == null) {
			throw new ScriptConfigurationException(
					"Context and/or scope are not initialized.");
		}
		for (SourceCode code : sourceCodes) {
			this.engine.put(ScriptEngine.FILENAME, code.getName());
			CompiledScript script = code.compile((Compilable) this.engine);
			ans = script.eval();
		}
		return ans;
	}
}
