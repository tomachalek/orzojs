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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.orzo.service.ScheduledTaskRunner;
import net.orzo.service.Task;
import net.orzo.service.TaskManager;

@Path("tasks")
public class TaskListHandler extends JsonProvider {

	private final TaskManager taskManager;

	/**
	 * 
	 */
	@Inject
	public TaskListHandler(TaskManager taskManager) {
		this.taskManager = taskManager;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public String getList() {
		Collection<Task> tasks = this.taskManager.getTasks();
		List<TaskInfo> sortedTasks = tasks
				.stream()
				.sorted((t1, t2) -> Long.compare(t1.getTimeCreated(),
						t2.getTimeCreated())).map((t) -> new TaskInfo(t))
				.collect(Collectors.toList());
		return toJson(sortedTasks);
	}

	class TaskInfo {

		public final String id;

		public final String name;

		public final long created;

		public final String status;

		public final boolean isScheduled;

		public final Integer startHour;

		public final Integer startMinute;

		public final Integer interval;


		TaskInfo(Task task) {
			this.id = task.getId();
			this.name = task.getName();
			this.created = task.getTimeCreated();
			this.status = task.getStatus().toString();
			if (TaskListHandler.this.taskManager.isScheduled(task)) {
				this.isScheduled = true;
				ScheduledTaskRunner str = TaskListHandler.this.taskManager.getSchedulingInfo(task);
				this.startHour = str.getStartHour();
				this.startMinute = str.getStartMinute();
				this.interval = str.getInterval();

			} else {
				this.isScheduled = false;
				this.startHour = null;
				this.startMinute = null;
				this.interval = null;
			}
		}
	}

}
