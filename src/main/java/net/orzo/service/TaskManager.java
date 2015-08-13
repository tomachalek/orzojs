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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.orzo.CalculationParams;
import net.orzo.Config;
import net.orzo.ScriptConfig;
import net.orzo.scripting.SourceCode;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Singleton
public class TaskManager {

	private final static String USERENV_PATH = "net/orzo/userenv.js";

	private final static String DATALIB_SCRIPT = "net/orzo/datalib.js";

	private final static String DEMO_SCRIPT = "net/orzo/demo1.js";

	private final static String CALCULATION_SCRIPT = "net/orzo/calculation.js";

	private final Map<String, Task> tasks;

	private final Config conf;

	/**
	 * 
	 * @param conf
	 */
	@Inject
	public TaskManager(Config conf) {
		this.conf = conf;
		this.tasks = new HashMap<String, Task>();
	}

	/**
	 * 
	 * @param taskId
	 * @return
	 * @throws ResourceNotAvailable
	 * @throws ResourceNotFound
	 */
	public Task getTask(String taskId) throws ResourceNotAvailable,
			ResourceNotFound {
		if (this.tasks.containsKey(taskId)) {
			return this.tasks.get(taskId);

		} else {
			throw new ResourceNotFound(String.format("Task %s not found",
					taskId));
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CalculationParams createDefaultCalculationParams()
			throws IOException {
		CalculationParams params = new CalculationParams();
		params.userenvScript = SourceCode.fromResource(USERENV_PATH);
		params.datalibScript = SourceCode.fromResource(DATALIB_SCRIPT);
		params.calculationScript = SourceCode.fromResource(CALCULATION_SCRIPT);
		return params;
	}
	
	/**
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean containsTask(String taskId) {
		return this.tasks.containsKey(taskId);
	}

	public void deleteTask(String taskId) throws ResourceNotFound {
		if (this.tasks.containsKey(taskId)) {
			this.tasks.remove(taskId);

		} else {
			throw new ResourceNotFound(String.format("Task %s not found",
					taskId));
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CalculationParams createDemoParams() throws IOException {
		CalculationParams params = createDefaultCalculationParams();
		params.userScript = SourceCode.fromResource(DEMO_SCRIPT);
		return params;
	}
	
	public String registerTask(String scriptId, String[] args)
			throws TaskException {
		if (!this.conf.isAllowedScript(scriptId)) {
			throw new RuntimeException("Script not allowed"); // TODO
		}
		String taskId = DigestUtils.sha1Hex(UUID.randomUUID().toString());
		ScriptConfig scriptConf = this.conf.getScriptPath(scriptId);

		File userScriptFile = new File(scriptConf.getScriptPath());
		try {
			CalculationParams params = createDefaultCalculationParams();
			params.userenvScript = SourceCode.fromResource(USERENV_PATH);
			if (scriptConf.getLibraryPath() != null) {
				params.optionalModulesPath = scriptConf.getLibraryPath();
			}
			params.userScript = SourceCode.fromFile(userScriptFile);
			params.workingDirModulesPath = userScriptFile.getParent();
			params.inputValues = args;
			this.tasks.put(taskId, new Task(params));
			return taskId;

		} catch (IOException ex) {
			throw new TaskException(ex.getMessage(), ex);
		}

	}

	/**
	 * 
	 * @param scriptId
	 * @return
	 * @throws TaskException
	 * @throws ResourceNotFound
	 */
	public void startTask(String taskId) throws TaskException, ResourceNotFound {
		if (this.tasks.containsKey(taskId)) {
			this.tasks.get(taskId).run();

		} else {
			throw new ResourceNotFound(String.format("task %s not found",
					taskId));
		}
	}

}
