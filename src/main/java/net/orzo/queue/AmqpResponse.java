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

import com.rabbitmq.client.Channel;
import net.orzo.service.AmqpConf;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class AmqpResponse implements ResponseClient {

    private final ChannelProvider channelProvider;

    private final AmqpConf conf;

    public AmqpResponse(ChannelProvider channelProvider, AmqpConf conf) {
        this.channelProvider = channelProvider;
        this.conf = conf;
    }

    @Override
    public void response(String message) {
        try {
            Channel ch = this.channelProvider.createChannel();
            ch.queueDeclare(this.conf.queue, true, false, false, null);
            ch.basicPublish("", this.conf.queue, null, message.getBytes());

        } catch (IOException e) {
            LoggerFactory.getLogger(AmqpResponse.class).error(
                    String.format("Failed to send response: %s", e.getMessage()));
        }
    }
}
