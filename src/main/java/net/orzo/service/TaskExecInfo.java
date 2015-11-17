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

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TaskExecInfo {

    public final String id;

    public final String name;

    public final long started;

    public final long finished;

    public final TaskStatus status;

    public final String err;

    /**
     * Dev. note - there should never be a direct reference
     * "TaskExecInfo->Task" as it would produce unnecessarily high
     * memory consumption when running as a service for some time.
     */
    public TaskExecInfo(String id, String name, long started, long finished,
                        TaskStatus status, String err) {
        super();
        this.id = id;
        this.name = name;
        this.started = started;
        this.finished = finished;
        this.status = status;
        this.err = err;
    }
}
