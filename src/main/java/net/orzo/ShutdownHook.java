/*
 * Copyright (c) 2015 Tomas Machalek
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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ShutdownHook extends Thread {

    /**
     *
     */
    private final App app;

    /**
     *
     */
    public ShutdownHook(App app) {
        this.app = app;
    }

    /**
     *
     */
    @Override
    public void run() {
        this.app.stopServices();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
