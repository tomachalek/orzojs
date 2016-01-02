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
package net.orzo.rest;

import net.orzo.service.ScheduledTaskRunner;
import net.orzo.service.Task;
import net.orzo.service.TaskManager;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TaskInfo {

    public final String id;

    public final String name;

    public final long created;

    public final String status;

    public final boolean isScheduled;

    public final Integer startHour;

    public final Integer startMinute;

    public final Integer interval;


    TaskInfo(TaskManager taskManager, Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.created = task.getTimeCreated();
        this.status = task.getStatus().toString();
        if (taskManager.isScheduled(task)) {
            this.isScheduled = true;
            ScheduledTaskRunner str = taskManager.getSchedulingInfo(task);
            this.startHour = str.getStartHour();
            this.startMinute = str.getStartMinute();
            this.interval = str.getInterval();

        } else {
            this.isScheduled = false;
            this.startHour = null;
            this.startMinute = null;
            this.interval = null;
        }
    }
}
