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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.orzo.Service;
import net.orzo.service.RedisConf;
import net.orzo.service.ServiceConfig;
import redis.clients.jedis.Jedis;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Singleton
public class RedisStorage implements ResultStorage, Service {

    private Jedis jedis;

    private final RedisConf conf;

    @Inject
    public RedisStorage(ServiceConfig conf) {
        this.conf = conf.getRedisConf();
    }


    @Override
    public void start() throws Exception {
        this.jedis = new Jedis(this.conf.host, this.conf.port);
        this.jedis.select(this.conf.db);
    }

    @Override
    public void stop() {
        this.jedis.close();
    }


    @Override
    public void set(String key, String value) {
        this.jedis.set(this.conf.taskKeyPrefix + key, value);
    }

    @Override
    public String get(String key) {
        return this.jedis.get(this.conf.taskKeyPrefix + key);
    }

    @Override
    public void delete(String key) {
        this.jedis.del(this.conf.taskKeyPrefix + key);
    }

    @Override
    public boolean isActive() {
        return this.jedis != null;
    }

}
