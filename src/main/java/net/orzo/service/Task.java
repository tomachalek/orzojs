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
import java.util.Observable;

import net.orzo.Calculation;
import net.orzo.CalculationParams;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Task extends Observable {

    private final String id;

    private final CalculationParams params;

    private final List<TaskEvent> events;

    private String result;

    public Task(String id, CalculationParams params) {
        super();
        this.id = id;
        this.params = params;
        this.events = new ArrayList<>();
        this.events.add(new TaskEvent(TaskStatus.PENDING));
    }

    public String getResult() throws ResourceNotAvailable {
        if (!getStatus().hasEnded()) {
            throw new ResourceNotAvailable("Result is not yet available");
        }
        return result;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.params.userScript.getName();
    }

    public TaskStatus getStatus() {
        return this.events.get(this.events.size() - 1).getStatus();
    }

    public TaskEvent getFirstError() {
        return this.events.stream()
                .filter((e) -> e.getStatus() == TaskStatus.ERROR).findFirst()
                .get();
    }

    public List<TaskEvent> getEvents() {
        return this.events;
    }

    public long getTimeCreated() {
        if (this.events.size() > 0) {
            return this.events.get(0).getCreated();
        }
        return -1;
    }

    public void addEvent(TaskEvent event) {
        this.events.add(event);
        setChanged();
        notifyObservers();
    }

    protected void run() {
        this.events.add(new TaskEvent(TaskStatus.RUNNING));
        Calculation proc = new Calculation(this.params, this::addEvent);
        this.result = proc.run();
    }

}
