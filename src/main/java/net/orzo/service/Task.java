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

import net.orzo.Calculation;
import net.orzo.CalculationException;
import net.orzo.CalculationParams;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class Task {

	private final CalculationParams params;

	private TaskStatus status;

	private String result;

	private TaskException error;

	public Task(CalculationParams params) {
		this.params = params;
		this.status = TaskStatus.PENDING;
	}

	public String getResult() throws ResourceNotAvailable {
		if (!this.status.hasEnded()) {
			throw new ResourceNotAvailable("Result is not yet available");
		}
		return result;
	}

	public String getName() {
		return this.params.userenvScript.getName();
	}

	public TaskStatus getStatus() {
		return this.status;
	}

	public TaskException getError() {
		return this.error;
	}

	protected void run() {
		this.status = TaskStatus.RUNNING;
		try {
			Calculation proc = new Calculation(params,
					(ts) -> Task.this.status = ts);
			Task.this.result = proc.run();

		} catch (CalculationException ex) {
			Task.this.status = TaskStatus.ERROR;
			Task.this.error = new TaskException(ex.getMessage(), ex);
		}

	}

}
