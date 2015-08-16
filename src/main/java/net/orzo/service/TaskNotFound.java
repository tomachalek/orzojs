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
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class TaskNotFound extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5109433240049842787L;

	public TaskNotFound(String message) {
		super(message);
	}

	public TaskNotFound(Throwable cause) {
		super(cause);
	}

	public TaskNotFound(String message, Throwable cause) {
		super(message, cause);
	}

}
