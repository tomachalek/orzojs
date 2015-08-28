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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.orzo.CalculationException;
import net.orzo.service.StatusResponse;
import net.orzo.service.TaskEvent;
import net.orzo.service.TaskManager;
import net.orzo.service.TaskStatus;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
@Path("result")
public class ResultHandler extends JsonProvider {

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
	 * @param e
	 * @return
	 */
	private List<Exception> getErrors(Exception e) {
		List<Exception> info = new ArrayList<Exception>();
		if (e instanceof CalculationException) {
			CalculationException cex = (CalculationException) e;
			for (Throwable e2 : cex.getAllErrors()) {
				info.add((Exception) e2);
			}

		} else {
			info.add((Exception) e.getCause());
		}
		return info;
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
				TaskEvent errEvent = task.getFirstError();
				return toJson(new StatusResponse(StatusResponse.Status.ERROR,
						"Action failed", errEvent.getErrors()));

			} else {
				return task.getResult();
			}

		} catch (Exception e) {
			return toJson(new StatusResponse(
					StatusResponse.Status.ERROR, e.getMessage(), getErrors(e)));
		}
	}

}
