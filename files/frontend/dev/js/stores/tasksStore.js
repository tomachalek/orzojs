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

import {GeneralStore} from './generalStore';
import * as $ from 'jquery';

export class TasksStore extends GeneralStore {

    constructor(dispatcher, mainStore) {
        super(dispatcher);
        this.dispatcherToken = this.dispatcher.register(this._handleEvents.bind(this));
        this.tasks = [];
        this.results = {};
        this.currentResult = null;
        this.mainStore = mainStore;
    }

    _handleEvents(payload) {
        switch (payload.type) {
            case 'TASKS_LOAD':
                this.synchronize();
                break;
            case 'TASK_RUN':
                this.runTask(payload.value);
                break;
            case 'TASK_SCHEDULE':
                this.scheduleTask(payload.value.taskId, payload.value.initHour,
                                  payload.value.interval);
                break;
            case 'TASK_DELETE':
                this.deleteTask(payload.value);
                break;
            case 'TASK_GET_RESULT':
                this.fetchResult(payload.value);
                break;
        }
    }

    getTasks() {
        return this.tasks;
    }

    getCurrentResult() {
        return this.results[this.currentResult];
    }

    runTask (taskId) {
        let self = this;
        let prom = $.ajax('/api/task/' + taskId, {
                    dataType: 'json',
                    method: 'POST'
                });

        prom.then(
            function (data) {
                self.notifyChangeListeners('TASK_RUN');
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err);
            }
        );
    }

    scheduleTask(taskId, startHour, interval) {
        let self = this;
        let prom = $.ajax('/api/task/' + taskId + '?time=' + startHour + '&interval=' + interval, {
                    dataType: 'json',
                    method: 'POST'
                });

        prom.then(
            function (data) {
                self.notifyChangeListeners('TASK_SCHEDULE');
                self.synchronize();
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err);
                self.synchronize();
            }
        );
    }

    deleteTask(taskId) {
        let self = this;
        let prom = $.ajax('/api/task/' + taskId, {
                    dataType: 'json',
                    method: 'DELETE'
                });

        prom.then(
            function (data) {
                self.notifyChangeListeners('TASK_DELETE');
                self.synchronize();
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err);
            }
        );
    }

    fetchResult(taskId) {
        let self = this;
        let prom = $.ajax('/api/result/' + taskId, {
                    dataType: 'json',
                    method: 'GET'
                });

        prom.then(
            function (data) {
                self.results[taskId] = data;
                self.currentResult = taskId;
                self.notifyChangeListeners('TASK_GET_RESULT');
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err);
            }
        );
    }

    synchronize() {
        var self = this;
        var prom = $.ajax('/api/tasks', {
            dataType: 'json',
            method: 'GET'
        });

        prom.then(
            function (data) {
                self.tasks = data;
                self.notifyChangeListeners('TASKS_LOAD');

            },
            function (error) {
                self.notifyChangeListeners('TASKS_LOAD', error);
            }
        );
    }
}