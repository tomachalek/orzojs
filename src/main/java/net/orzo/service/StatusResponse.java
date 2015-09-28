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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple status wrapper used as a REST response in these cases: 1)
 * void-returning methods in both OK/ERROR situations 2) methods returning data
 * in case of an error
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class StatusResponse {

	public enum Status {
		OK("OK"), ERROR("ERROR");
		
		private final String repr;
		
		private Status(String repr) {
			this.repr = repr;
		}

		@Override
		public String toString() {
			return this.repr;
		}
	}

	public final Status status;

	public final String message;

	public final List<String> errors;

	/**
	 * 
	 * @param status
	 * @param message
	 */
	public StatusResponse(Status status, String message,
			List<? extends Throwable> errors) {
		this.status = status;
		this.message = message;
		this.errors = errors.stream().map((e) -> e.getMessage())
				.collect(Collectors.toList());
	}
	
	public StatusResponse(Status status, String message, Throwable error) {
		this.status = status;
		this.message = message;
		this.errors = new ArrayList<>();
		this.errors.add(error.getMessage());
	}

	public StatusResponse(Status status) {
		this.status = status;
		this.message = null;
		this.errors = Collections.emptyList();
	}

}
