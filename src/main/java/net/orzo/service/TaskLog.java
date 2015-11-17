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

import java.util.ArrayList;
import java.util.List;

/**
 * This object logs tasks knowing what their events mean. It stores only
 * primitive values to prevent keeping references to calculation objects.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TaskLog {

    private final List<TaskExecInfo> rows;

    public TaskLog() {
        this.rows = new ArrayList<>();
    }

    public void logTask(Task task) {
        long started = -1;
        long finished = -1;
        String err = null;

        for (TaskEvent event : task.getEvents()) {
            if (event.getStatus() == TaskStatus.RUNNING) {
                started = event.getCreated();

            } else if (event.getStatus() == TaskStatus.FINISHED) {
                finished = event.getCreated();

            } else if (event.getStatus() == TaskStatus.ERROR) {
                finished = event.getCreated();
                TaskEvent errEvent = task.getFirstError();
                if (errEvent.getErrors().size() > 0) {
                    err = errEvent.getErrors().get(0).getMessage();
                }
            }
        }
        this.rows.add(new TaskExecInfo(task.getId(), task.getName(), started,
                finished, task.getStatus(), err));
    }

    public List<TaskExecInfo> getData() {
        return this.rows;
    }
}
