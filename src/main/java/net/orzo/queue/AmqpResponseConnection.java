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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.orzo.Service;
import net.orzo.service.AmqpConf;
import net.orzo.service.ServiceConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
@Singleton
public class AmqpResponseConnection implements Service, ChannelProvider {

    private Connection connection;

    private final AmqpConf conf;

    @Inject
    public AmqpResponseConnection(ServiceConfig conf) {
        this.conf = conf.getAmqpConfig();
    }

    @Override
    public Channel createChannel() throws IOException {
        Channel ch = this.connection.createChannel();
        ch.queueDeclare(this.conf.queue, true, false, false, null);
        return ch;
    }

    @Override
    public boolean isActive() {
        return this.connection != null;
    }

    @Override
    public void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.conf.host);
        factory.setPort(this.conf.port);
        factory.setVirtualHost(this.conf.virtualHost);
        factory.setUsername(this.conf.user);
        factory.setPassword(this.conf.password);
        this.connection = factory.newConnection();
    }

    @Override
    public void stop() {
        try {
            this.connection.close();

        } catch (IOException e) {
            LoggerFactory.getLogger(AmqpConnection.class).error(e.getMessage());
        }
    }
}