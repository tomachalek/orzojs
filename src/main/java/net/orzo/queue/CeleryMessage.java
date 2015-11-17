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

package net.orzo.queue;

import java.util.List;
import java.util.Map;


/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class CeleryMessage {

    public String id;

    public String task;

    public List<String> args;

    public Map<String, String> kwargs;

    public Integer retries;

    public String eta;

    public String expires;

    @Override
    public String toString() {
        return String.format("CeleryMessage {id: %s, task: %s, args: %s, kwargs: %s, ...",
                this.id, this.task, this.args, this.kwargs);
    }

}
