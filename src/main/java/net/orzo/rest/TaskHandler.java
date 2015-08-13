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

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.orzo.service.ResourceNotFound;
import net.orzo.service.StatusResponse;
import net.orzo.service.TaskException;
import net.orzo.service.TaskManager;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Path("task")
public class TaskHandler
{
	
	private final TaskManager taskManager;
	
	/**
	 * 
	 */
	@Inject
	public TaskHandler(TaskManager taskManager)
	{
		this.taskManager = taskManager;
	}
	
	
	/**
	 * 
	 */
	@PUT
	@Path("{script}")
	@Produces("text/plain; charset=UTF-8")
	public String registerTask(@PathParam("script") String scriptId,
			@QueryParam("arg") List<String> args) {

		try {
			return this.taskManager.registerTask(scriptId,
					args.toArray(new String[args.size()]));
		} catch (TaskException e) {
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage()));
		}
	}
	
	@POST
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String runTask(@PathParam("task") String taskId) {
		try {
			this.taskManager.startTask(taskId);
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.OK, null));

		} catch (TaskException | ResourceNotFound e) {
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage()));
		}
	}

	@DELETE
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String deleteResult(@PathParam("task") String taskId) {
		try {
			this.taskManager.deleteTask(taskId);
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.OK, null));

		} catch (ResourceNotFound e) {
			return new Gson().toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage()));

		}
	}
}
