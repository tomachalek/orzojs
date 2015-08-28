package net.orzo.rest;

import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.orzo.service.ScriptConfig;
import net.orzo.service.TaskManager;

@Path("scripts")
public class ScriptListHandler extends JsonProvider {

	private final TaskManager taskManager;

	/**
	 * 
	 */
	@Inject
	public ScriptListHandler(TaskManager taskManager) {
		super();
		this.taskManager = taskManager;
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	public String getList() {
		return toJson(this.taskManager.getScriptsIds().stream()
				.map((item) -> new ScriptInfo(item, this.taskManager.getScriptConfig(item)))
				.collect(Collectors.toList()));
	}

	class ScriptInfo {

		public final String id;

		public final String name;

		public final String[] defaultArgs;

		public final String description;

		public ScriptInfo(String scriptId, ScriptConfig conf) {
			this.id = scriptId;
			this.name = conf.getScript().getName();
			this.description = conf.getDescription();
			this.defaultArgs = conf.getDefaultArgs();
		}
	}

}
