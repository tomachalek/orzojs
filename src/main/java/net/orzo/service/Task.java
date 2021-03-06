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
import java.util.Observer;

import net.orzo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Task extends Observable implements Observer {

    private final String id;

    private final CalculationParams params;

    private final List<TaskEvent> events;

    private final SharedServices sharedServices;

    private Object result;

    private static final Logger LOG = LoggerFactory
            .getLogger(Task.class);

    public Task(String id, CalculationParams params, SharedServices sharedServices) {
        super();
        this.id = id;
        this.params = params;
        this.sharedServices = sharedServices;
        this.events = new ArrayList<>();
        this.events.add(new TaskEvent(TaskStatus.PENDING));
    }

    public Object getResult() throws ResourceNotAvailable {
        if (!getStatus().isFinal()) {
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

    public long getProcessingTime() {
        long from = getTimeCreated();
        if (from > - 1
                && this.events.size() > 1
                && this.events.get(this.events.size() - 1).getStatus().isFinal()) {
            return this.events.get(this.events.size() - 1).getCreated() - from;
        }
        return -1;
    }

    public void addEvent(TaskEvent event) {
        this.events.add(event);
        setChanged();
        notifyObservers();
    }

    protected void run() {
        this.events.add(new TaskEvent(TaskStatus.PREPARING));
        Calculation proc = new Calculation(this.params, this.sharedServices);
        proc.addObserver(this);
        try {
            this.result = proc.run();
            addEvent(new TaskEvent(TaskStatus.FINISHED));
            LOG.info("Processing time: " + Util.milliSecondsToHMS(getProcessingTime()));

        } catch (CalculationException e) {
            addEvent(new TaskEvent(TaskStatus.ERROR, e));
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TaskEvent) {
            addEvent((TaskEvent)arg);
        }
    }
}
