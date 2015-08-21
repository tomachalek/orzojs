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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.orzo.CalculationParams;
import net.orzo.scripting.SourceCode;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final static String CALCULATION_SCRIPT = "net/orzo/calculation.js";

	private final Map<String, Task> tasks;

	private final ServiceConfig conf;

	private final ScheduledExecutorService scheduler;

	private final Map<Task, ScheduledFuture<?>> schedules;
	
	private static final Logger LOG = LoggerFactory
			.getLogger(TaskManager.class);

	/**
	 * 
	 * @param conf
	 */
	@Inject
	public TaskManager(ServiceConfig conf) {
		this.conf = conf;
		this.tasks = new HashMap<String, Task>();
		this.scheduler = Executors.newScheduledThreadPool(1); // TODO size
		this.schedules = new HashMap<Task, ScheduledFuture<?>>();
	}

	/**
	 * 
	 * @param taskId
	 * @return
	 */
	public Task getTask(String taskId) {
		if (this.tasks.containsKey(taskId)) {
			return this.tasks.get(taskId);

		} else {
			throw new TaskNotFound(String.format("Task %s not found", taskId));
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

	/**
	 * 
	 * @param taskId
	 * @throws TaskNotFound
	 */
	public void deleteTask(String taskId) throws ResourceNotFound {
		Task t = getTask(taskId);
		if (this.schedules.containsKey(t)) {
			this.schedules.get(taskId).cancel(false); // TODO may interrupt
														// ?
		}
		this.tasks.remove(taskId);
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
			params.userScript = scriptConf.getScript();
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
	public void startTask(String taskId) throws ResourceNotFound {
		if (this.tasks.containsKey(taskId)) {
			new Thread() {
				@Override
				public void run() {
					TaskManager.this.tasks.get(taskId).run();
				}
			}.start();

		} else {
			throw new ResourceNotFound(String.format("task %s not found",
					taskId));
		}
	}

	/**
	 * 
	 * @param taskId
	 */
	public void startTaskSync(String taskId) {
		getTask(taskId).run();
	}

	/**
	 * 
	 * @param hours
	 * @param minutes
	 * @return
	 */
	private long calculateInitialDelay(int hours, int minutes) {
		Calendar startDate = Calendar.getInstance();
		Calendar currDate = Calendar.getInstance();
		if (3600 * hours + 60 * minutes <= 3600
				* startDate.get(Calendar.HOUR_OF_DAY) + 60
				* startDate.get(Calendar.MINUTE)) {
			startDate.add(Calendar.DAY_OF_MONTH, 1);
		}
		startDate.set(Calendar.HOUR_OF_DAY, hours);
		startDate.set(Calendar.MINUTE, minutes);
		startDate.set(Calendar.SECOND, 0);
		return startDate.getTimeInMillis() - currDate.getTimeInMillis();
	}

	/**
	 * 
	 * @param taskId
	 * @param startHour
	 * @param startMinute
	 * @param interval
	 */
	public void scheduleTask(String taskId, int startHour, int startMinute,
			int interval) {
		final Runnable runner = new Runnable() {

			public void run() {
				LOG.info("Running scheduled task "
						+ TaskManager.this.tasks.get(taskId).getName());
				TaskManager.this.tasks.get(taskId).run();
			} 			
		};
		
		ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(runner,
				calculateInitialDelay(startHour, startMinute),
				interval * 1000, TimeUnit.MILLISECONDS);
		this.schedules.put(this.tasks.get(taskId), future);
	}



}
