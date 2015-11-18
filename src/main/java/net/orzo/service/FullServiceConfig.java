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
import java.util.Map;


import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

/**
 * Stores a configuration of a script registered during start of the service.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Singleton
public class FullServiceConfig implements ServiceConfig {

    private String httpHost;

    private short httpPort;

    private Map<String, TaskScriptConfig> allowedScripts;

    private AmqpConf amqp;

    private AmqpConf amqpResponse;

    private RedisConf redis;

    @Override
    public boolean isAllowedScript(String id) {
        return this.allowedScripts.containsKey(id);
    }

    @Override
    public TaskScriptConfig getScriptConfig(String id) {
        return this.allowedScripts.get(id);
    }

    public String getHttpHost() {
        return this.httpHost;
    }

    public short getHttpPort() {
        return this.httpPort;
    }

    @Override
    public String toString() {
        return String.format("Config {httpHost: %s, httpPort: %s, %s}",
                this.httpHost, this.httpPort,
                StringUtils.join(this.allowedScripts.values(), ", "));
    }

    @Override
    public List<String> getScriptsIds() {
        List<String> ans = new ArrayList<>(this.allowedScripts.keySet());
        Collections.sort(ans);
        return ans;
    }

    @Override
    public AmqpConf getAmqpConfig() {
        return this.amqp;
    }

    @Override
    public AmqpConf getAmqpResponseConfig() {
        return this.amqpResponse;
    }

    @Override
    public RedisConf getRedisConf() {
        return this.redis;
    }

}
