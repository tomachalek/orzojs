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

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import net.orzo.service.ResourceNotAvailable;
import net.orzo.service.Task;
import net.orzo.service.TaskManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class TaskConsumer extends DefaultConsumer {

    private final TaskManager taskManager;

    private final ResponseClient responseClient;

    private final ResultStorage resultStorage;


    public TaskConsumer(Channel channel, ResponseClient responseClient,
                        ResultStorage resultStorage, TaskManager taskManager) {
        super(channel);
        this.responseClient = responseClient;
        this.resultStorage = resultStorage;
        this.taskManager = taskManager;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body)
            throws IOException {
        try {
            String rawMessage = new String(body, "utf-8");
            CeleryMessage msg = new Gson().fromJson((rawMessage), CeleryMessage.class);

            Observer obs = (Observable o, Object arg) -> {
                String result;
                try {
                    getChannel().basicAck(envelope.getDeliveryTag(), false);

                    if (o instanceof Task) {
                        result = new Gson().toJson(((Task)o).getResult());

                        this.responseClient.response(result);

                        if (this.resultStorage.isActive()) {
                            this.resultStorage.set(msg.id, result);
                        }
                    }

                } catch (IOException e) {
                    LoggerFactory.getLogger(TaskConsumer.class).error(
                            String.format("Failed to acknowledge sender: %s", e.getMessage()));

                } catch (ResourceNotAvailable e) {
                    LoggerFactory.getLogger(TaskConsumer.class).error(
                            String.format("Failed to send response message: %s", e.getMessage()));
                }
            };

            if (msg.task != null) {
                String taskId = this.taskManager.registerTask(msg.task,
                        msg.args.toArray(new String[msg.args.size()]), obs);
                this.taskManager.startTask(taskId);

            } else {
                throw new IllegalArgumentException(
                        String.format("Invalid message: %s", rawMessage));
            }

        } catch (Exception e) {
            LoggerFactory.getLogger(TaskConsumer.class).error(e.getMessage());
            getChannel().basicAck(envelope.getDeliveryTag(), false); // to get rid of invalid message
        }
    }
}
