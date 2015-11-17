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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.orzo.CalculationParams;
import net.orzo.scripting.SourceCode;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Singleton
public class TaskManager implements Observer {

    private final static String USERENV_PATH = "net/orzo/userenv.js";

    private final static String DATALIB_SCRIPT = "net/orzo/datalib.js";

    private final static String CALCULATION_SCRIPT = "net/orzo/calculation.js";

    private final Map<String, Task> tasks;

    private final ServiceConfig conf;

    private final ScheduledExecutorService scheduler;

    private final Map<Task, ScheduledTaskRunner> schedules;

    private final TaskLog execLog;

    /**
     */
    @Inject
    public TaskManager(ServiceConfig conf) {
        this.conf = conf;
        this.tasks = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1); // TODO size
        this.schedules = new HashMap<>();
        this.execLog = new TaskLog();
    }

    /**
     */
    public Task getTask(String taskId) {
        if (this.tasks.containsKey(taskId)) {
            return this.tasks.get(taskId);

        } else {
            throw new TaskNotFound(String.format("Task %s not found", taskId));
        }
    }

    public Collection<Task> getTasks() {
        return this.tasks.values();
    }

    /**
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
     */
    public boolean containsTask(String taskId) {
        return this.tasks.containsKey(taskId);
    }

    /**
     * @throws TaskNotFound
     */
    public void deleteTask(String taskId) throws ResourceNotFound {
        Task task = getTask(taskId);
        if (this.schedules.containsKey(task)) {
            this.schedules.get(task).cancel(); // TODO may interrupt
            // ?
        }
        this.tasks.remove(taskId);
    }

    public String registerTask(String scriptId, String[] args, Observer onFinished)
            throws TaskException {
        if (!this.conf.isAllowedScript(scriptId)) {
            throw new RuntimeException("Script not allowed"); // TODO
        }
        String taskId = DigestUtils.sha1Hex(UUID.randomUUID().toString());
        ScriptConfig scriptConf = this.conf.getScriptConfig(scriptId);
        File userScriptFile = new File(scriptConf.getScriptPath());
        Task task;

        try {
            CalculationParams params = createDefaultCalculationParams();
            params.userenvScript = SourceCode.fromResource(USERENV_PATH);
            if (scriptConf.getLibraryPath() != null) {
                params.optionalModulesPath = scriptConf.getLibraryPath();
            }
            params.userScript = scriptConf.getScript();
            params.workingDirModulesPath = userScriptFile.getParent();
            params.inputValues = args != null && args.length > 0 ? args : scriptConf.getDefaultArgs();
            task = new Task(taskId, params);
            task.addObserver(this);
            if (onFinished != null) {
                task.addObserver(onFinished);
            }
            this.tasks.put(taskId, task);
            return taskId;

        } catch (IOException ex) {
            throw new TaskException(ex.getMessage(), ex);
        }
    }

    public String registerTask(String scriptId, String[] args) throws TaskException {
        return registerTask(scriptId, args, null);
    }

    /**
     *
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
     */
    public void startTaskSync(String taskId) {
        getTask(taskId).run();
    }


    /**
     */
    public void scheduleTask(String taskId, int startHour, int startMinute,
                             int interval) {
        if (this.schedules.containsKey(taskId)) {
            throw new TaskSchedulingException("Task already scheduled. Please use/create another task.");
        }

        ScheduledTaskRunner scheduledTask = new ScheduledTaskRunner(this.scheduler, startHour, startMinute, interval);
        this.schedules.put(this.tasks.get(taskId), scheduledTask);
        scheduledTask.start(this.tasks.get(taskId));
    }

    public boolean isScheduled(Task task) {
        return this.schedules.containsKey(task);
    }

    public ScheduledTaskRunner getSchedulingInfo(Task task) {
        if (isScheduled(task)) {
            return this.schedules.get(task);

        } else {
            throw new TaskNotFound(String.format("Task %s is not scheduled.", task.getId()));
        }
    }

    @Override
    public void update(Observable obj, Object arg1) {
        if (obj instanceof Task) {
            this.execLog.logTask((Task) obj);
        }
    }

    public TaskLog getExecLog() {
        return this.execLog;
    }

    public List<String> getScriptsIds() {
        return this.conf.getScriptsIds();
    }

    public ScriptConfig getScriptConfig(String id) {
        return this.conf.getScriptConfig(id);
    }


}
