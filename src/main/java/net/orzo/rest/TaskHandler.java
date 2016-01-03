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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.*;

import com.google.inject.Inject;

import net.orzo.service.ArgumentException;
import net.orzo.service.ResourceNotFound;
import net.orzo.service.StatusResponse;
import net.orzo.service.TaskException;
import net.orzo.service.TaskManager;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Path("task")
public class TaskHandler extends JsonProvider
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
	public String registerTask(@PathParam("script") String scriptId, @FormParam("arg[]") List<String> args) {
		try {
			return this.taskManager.registerTask(scriptId,
					args.toArray(new String[args.size()]));
		} catch (TaskException e) {
			return toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage(), e.getCause()));
		}
	}
	
	@POST
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String runTask(@PathParam("task") String taskId,
			@QueryParam("time") @DefaultValue("") String time,
			@QueryParam("interval") @DefaultValue("0") String interval) {
		try {
			if (!time.equals("") && interval != null) {
				Pattern ptr = Pattern.compile("([0-2]?[0-9]):([0-5][0-9])");
				Matcher match = ptr.matcher(time);
				if (!match.matches()) {
					throw new ArgumentException("Invalid time format");
				}
				int startHour = Integer.parseInt(match.group(1));
				int startMinute = Integer.parseInt(match.group(2));
                int taskInterval = Integer.parseInt(interval);
				this.taskManager.scheduleTask(taskId, startHour, startMinute, taskInterval);

			} else {
				this.taskManager.startTask(taskId);
			}
			return toJson(new StatusResponse(
					StatusResponse.Status.OK));

		} catch (ResourceNotFound | ArgumentException | NumberFormatException e) {
			return toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage(), e));
		}
	}

	@DELETE
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String deleteTask(@PathParam("task") String taskId) {
		try {
			this.taskManager.deleteTask(taskId);
			return toJson(new StatusResponse(
					StatusResponse.Status.OK));

		} catch (ResourceNotFound e) {
			return toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage(), e));

		}
	}

	@GET
	@Path("{task}")
	@Produces("application/json; charset=UTF-8")
	public String getTask(@PathParam("task") String taskId) {
		return toJson(new TaskInfo(this.taskManager, this.taskManager.getTask(taskId)));
	}
}
