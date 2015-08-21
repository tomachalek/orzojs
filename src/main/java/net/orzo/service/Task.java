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

import net.orzo.Calculation;
import net.orzo.CalculationParams;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class Task {

	private final CalculationParams params;

	private final List<TaskEvent> events;

	private String result;

	public Task(CalculationParams params) {
		this.params = params;
		this.events = new ArrayList<TaskEvent>();
		this.events.add(new TaskEvent(TaskStatus.PENDING));
	}

	public String getResult() throws ResourceNotAvailable {
		if (!getStatus().hasEnded()) {
			throw new ResourceNotAvailable("Result is not yet available");
		}
		return result;
	}

	public String getName() {
		return this.params.userenvScript.getName();
	}

	public TaskStatus getStatus() {
		return this.events.get(this.events.size() - 1).getStatus();
	}

	public TaskEvent getFirstError() {
		return this.events.stream()
				.filter((e) -> e.getStatus() == TaskStatus.ERROR).findFirst()
				.get();
	}

	protected void run() {
		this.events.add(new TaskEvent(TaskStatus.RUNNING));
		Calculation proc = new Calculation(params,
				(taskEvent) -> this.events.add(taskEvent));
		this.result = proc.run();
	}

}
