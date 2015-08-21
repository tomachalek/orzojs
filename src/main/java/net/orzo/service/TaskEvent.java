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

import net.orzo.CalculationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class TaskEvent {

	private final TaskStatus status;

	private final long created;

	private final List<Exception> errors;

	private static final Logger LOG = LoggerFactory.getLogger(TaskEvent.class);

	public TaskEvent(TaskStatus status) {
		this.created = System.currentTimeMillis();
		this.status = status;
		LOG.info(String.format("Created event <%s>", this.status));
		this.errors = new ArrayList<Exception>();
	}

	public TaskEvent(TaskStatus status, CalculationException error) {
		this(status);
		this.errors.addAll(error.getAllErrors());
	}

	public TaskEvent(TaskStatus status, Exception error) {
		this(status);
		this.errors.add(error);
	}


	public TaskStatus getStatus() {
		return this.status;
	}

	public long getCreated() {
		return this.created;
	}

	public List<Exception> getErrors() {
		return this.errors;
	}

}
