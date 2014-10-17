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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.orzo.IntermediateResults;
import net.orzo.ScriptGlobalFuncHandler;
import net.orzo.lib.Lib;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

/**
 * Javascript engine wrapper to serve within the application. It is expected to
 * be used within a single thread (i.e. each thread must have its own instance
 * if needed).
 * 
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class JsEngineFacade {

	/**
	 * 
	 */
	private final ScriptGlobalFuncHandler funcHandler;

	/**
	 * 
	 */
	private Lib system;

	/**
	 * 
	 */
	private Context context;

	/**
	 * 
	 */
	private ScriptableObject scope;

	/**
	 * 
	 */
	private final EnvParams envParams;

	/**
	 * 
	 */
	private IntermediateResults intermediateResults;

	/**
	 * @param envParams
	 * @param funcHandler
	 * @param intermediateResults
	 */
	public JsEngineFacade(EnvParams envParams,
			ScriptGlobalFuncHandler funcHandler,
			IntermediateResults intermediateResults) {
		this.envParams = envParams;
		this.funcHandler = funcHandler;
		this.intermediateResults = intermediateResults;
	}

	/**
	 * 
	 */
	public JsEngineFacade(EnvParams envParams,
			ScriptGlobalFuncHandler funcHandler) {
		this(envParams, funcHandler, null);
	}

	/**
	 * 
	 */
	public void beginWork() {
		this.context = Context.enter();
		this.scope = new ImporterTopLevel(this.context);
		this.system = new Lib(this.scope);
		initModuleSubsystem();

		// objects with the "_" prefix are not intended to be used directly from
		// within javascript
		this.scope.put("_mr", this.scope, this.funcHandler);
		this.scope.put("_lib", this.scope, this.system);
		this.scope.put("_env", this.scope, this.envParams);
		if (this.intermediateResults != null) {
			this.scope.put("_result", this.scope, this.intermediateResults);
		}
	}

	/**
	 * 
	 */
	public void endWork() {
		Context.exit();
	}

	/**
	 * Sets object within the scripting environment
	 * 
	 */
	public void put(String key, Object value) {
		Object wrappedOut = Context.javaToJS(value, this.scope);
		ScriptableObject.putProperty(this.scope, key, wrappedOut);
	}

	/**
	 * Retrieves a value from the scripting environment
	 * 
	 */
	public Object get(String key) {
		return this.scope.get(key, this.scope);
	}

	/**
	 * Runs specified source code without any implicit imports.
	 * 
	 * @throws ScriptProcessingException
	 * 
	 */
	public Object runCode(SourceCode[] sourceCodes)
			throws ScriptProcessingException {
		Object ans = null;
		if (this.context == null || this.scope == null) {
			throw new ScriptConfigurationException(
					"Context and/or scope are not initialized.");
		}
		try {
			for (SourceCode code : sourceCodes) {
				ans = this.context.evaluateString(this.scope,
						code.getContents(), code.getId(), 1, null);
			}

		} catch (EvaluatorException e) {
			throw new ScriptProcessingException(e.getMessage(), e);

		} catch (EcmaError e) {
			throw new ScriptProcessingException(e.getMessage(), e);
		}
		return ans;
	}

	/**
	 * @throws ScriptProcessingException
	 * 
	 */
	public Object runCode(SourceCode sourceCode)
			throws ScriptProcessingException {
		return runCode(new SourceCode[] { sourceCode });
	}

	/**
	 * 
	 */
	public boolean containsFunction(String name) {
		Object callee = this.scope.get(name, this.scope);
		return callee != null && callee instanceof Function;
	}

	/**
	 * @throws NoSuchMethodException
	 * 
	 */
	public Object invokeFunction(String name, Object... args)
			throws NoSuchMethodException {
		Object callee = this.scope.get(name, this.scope);
		if (!(callee instanceof Function)) {
			throw new TypeHandlingException(String.format(
					"Object %s is not a Function type", name));
		}
		Function fct = (Function) this.scope.get(name, this.scope);
		Object result = fct.call(this.context, this.scope, this.scope, args);
		return result;
	}

	/**
	 * @throws NoSuchMethodException
	 * 
	 */
	public Object invokeMethod(String objectName, String methodName,
			Object... args) throws NoSuchMethodException {
		Object callee = get(objectName);
		if (!(callee instanceof NativeObject)) {
			throw new TypeHandlingException(String.format(
					"Value %s is not an Object type", objectName));
		}
		NativeObject obj = (NativeObject) get(objectName);
		return ScriptableObject.callMethod(obj, methodName, args);
	}

	/**
	 * 
	 */
	public Context getContext() {
		return this.context;
	}

	/**
	 * 
	 */
	public ScriptableObject getScope() {
		return this.scope;
	}

	/**
	 * Initializes support for CommonJS modules
	 * 
	 * NOTE: this code is from the Rhino project source
	 */
	private void initModuleSubsystem() {
		RequireBuilder rb = new RequireBuilder();
		rb.setSandboxed(true);
		List<URI> uris = new ArrayList<URI>();

		if (this.envParams.modulesPaths != null) {
			for (String path : this.envParams.modulesPaths) {
				try {
					URI uri = new File(path).toURI();
					if (!uri.isAbsolute()) {
						uri = uri.resolve("");
					}
					if (!uri.toString().endsWith("/")) {
						uri = new URI(uri + "/");
					}
					uris.add(uri);
				} catch (URISyntaxException usx) {
					throw new RuntimeException(usx);
				}
			}
		}
		rb.setModuleScriptProvider(new SoftCachingModuleScriptProvider(
				new UrlModuleSourceProvider(uris, null)));
		Require require = rb.createRequire(this.context, this.scope);
		require.install(this.scope);
	}

}
