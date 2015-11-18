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

package net.orzo;

import java.util.ArrayList;
import java.util.List;

import net.orzo.scripting.SourceCode;
import net.orzo.service.*;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class CmdConfig implements ServiceConfig {

    private final String scriptId;

    private final InternalScriptConfig scriptConfig;

    public CmdConfig(String scriptId, SourceCode userScript, String libPath) {
        this.scriptId = scriptId;
        this.scriptConfig = new InternalScriptConfig(userScript, libPath);
    }

    @Override
    public boolean isAllowedScript(String id) {
        return this.scriptId.equals(id);
    }

    @Override
    public ScriptConfig getScriptConfig(String id) {
        if (isAllowedScript(id)) {
            return this.scriptConfig;
        }
        return null;
    }

    @Override
    public List<String> getScriptsIds() {
        List<String> ans = new ArrayList<>();
        ans.add(this.scriptId);
        return ans;
    }

    @Override
    public AmqpConf getAmqpConfig() {
        return null;
    }

    @Override
    public AmqpConf getAmqpResponseConfig() {
        return null;
    }

    @Override
    public RedisConf getRedisConf() {
        return null;
    }


}
