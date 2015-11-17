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
import com.google.inject.name.Named;
import com.rabbitmq.client.Channel;
import net.orzo.Service;
import net.orzo.service.AmqpConf;
import net.orzo.service.ServiceConfig;
import net.orzo.service.TaskManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class AmqpService implements Service {

    private final ChannelProvider channelProvider;

    private final ChannelProvider responseChannelProvider;

    private final TaskManager taskManager;

    private final ServiceConfig conf;

    private Channel channel;

    private TaskConsumer taskConsumer;


    @Inject
    public AmqpService(@Named("receiver") ChannelProvider channelProvider,
                       @Named("responder") ChannelProvider responseChannelProvider,
                       TaskManager taskManager, ServiceConfig conf) {
        super();
        this.channelProvider = channelProvider;
        this.responseChannelProvider = responseChannelProvider;
        this.taskManager = taskManager;
        this.conf = conf;
    }

    @Override
    public void start() throws Exception {
        this.channel = this.channelProvider.createChannel();
        if (this.conf.getAmqpConfig().qos != null) {
            this.channel.basicQos(this.conf.getAmqpConfig().qos);
        }

        ResponseClient responseClient;
        if (this.responseChannelProvider.isActive()) {
            responseClient = new AmqpResponse(responseChannelProvider, this.conf.getAmqpResponseConfig());

        } else {
            responseClient = new DummyResponse();
        }

        this.taskConsumer = new TaskConsumer(this.channel, responseClient, this.taskManager);
        AmqpConf confIn = this.conf.getAmqpConfig();
        this.channel.basicConsume(confIn.queue, confIn.autoAcknowledge, this.taskConsumer);
    }

    @Override
    public void stop() {
        try {
            this.channel.close();

        } catch (IOException | TimeoutException e) {
            LoggerFactory.getLogger(AmqpService.class).error(e.getMessage());
        }
    }
}
