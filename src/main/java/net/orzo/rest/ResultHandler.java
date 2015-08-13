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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.orzo.service.ResourceNotAvailable;
import net.orzo.service.ResourceNotFound;
import net.orzo.service.StatusResponse;
import net.orzo.service.TaskException;
import net.orzo.service.TaskManager;
import net.orzo.service.TaskStatus;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
@Path("result")
public class ResultHandler {

	private final TaskManager taskManager;

	/**
	 * 
	 */
	@Inject
	public ResultHandler(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	/**
	 * 
	 */
	@GET
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String getResult(@PathParam("task") String taskId) {
		try {
			net.orzo.service.Task task = this.taskManager.getTask(taskId);
			if (task.getStatus() == TaskStatus.ERROR) {
				if (task.getError() != null) {
					throw task.getError();

				} else {
					throw new TaskException("Unknown task error");
				}

			} else {
				return task.getResult();
			}

		} catch (ResourceNotAvailable | ResourceNotFound | TaskException e) {
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage()));
		}
	}

}
