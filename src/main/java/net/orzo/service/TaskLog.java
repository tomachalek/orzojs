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

    private final List<TaskEventInfo> rows;

    public TaskLog() {
        this.rows = new ArrayList<>();
    }

    public void logTask(Task task) {
        Long created = null;
        String err = null;

        for (TaskEvent event : task.getEvents()) {
            if (event.getStatus() == TaskStatus.ERROR) {
                created = event.getCreated();
                TaskEvent errEvent = task.getFirstError();
                if (errEvent.getErrors().size() > 0) {
                    err = errEvent.getErrors().get(0).getMessage();
                }

            } else {
                created = event.getCreated();
            }
        }
        this.rows.add(new TaskEventInfo(this.rows.size(), task.getId(), task.getName(), created,
                task.getStatus(), err));
    }

    public List<TaskEventInfo> getData() {
        return this.rows;
    }
}
