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


public class AmqpConf {

    public String host;

    public Integer port;

    public String virtualHost;

    public String queue;

    public String user;

    public String password;

    public Boolean autoAcknowledge;

    @Override
    public String toString() {
        return String.format("%s:****@%s:%s/%s, (queue: %s, autoAck: %s)", this.user, this.host,
                this.port, this.virtualHost, this.queue, this.autoAcknowledge);
    }
}