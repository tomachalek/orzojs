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

import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.orzo.service.ScriptConfig;
import net.orzo.service.TaskManager;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
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
