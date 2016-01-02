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

export class ScriptsStore extends GeneralStore {

    constructor(dispatcher, tasksStore, mainStore) {
        super(dispatcher);
        this.tasksStore = tasksStore;
        this.mainStore = mainStore;
        this.data = [];
        this.defaultArgs = {};
        this.dispatcherToken = this.dispatcher.register(this.handleEvent.bind(this));
    }

    handleEvent(payload) {
        switch (payload.type) {
            case 'SCRIPTS_LOAD':
                this.loadList();
                break;
            case 'SCRIPT_ACTION_RUN':
                this.runScript(payload.value.itemId, payload.value.args);
                break;
            case 'SCRIPT_ACTION_REGISTER':
                this.registerTask(payload.value.itemId, payload.value.args);
                break;
        }
    }

    loadList() {
        let prom = $.ajax('/api/scripts', {
            dataType: 'json',
            method: 'GET'
        });
        let self = this;

        prom.then(
            function (data) {
                data.forEach((item) => {
                    self.defaultArgs[item.id] = item.defaultArgs;
                });
                self.data = data;
                self.notifyChangeListeners('SCRIPTS_LOAD');
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err.statusText || err);
                self.mainStore.addMessage('error', err.statusText || err);
            }
        );
    }

    getDefaultArgs(itemId) {
        return this.defaultArgs[itemId] || [];
    }

    // TODO this should remove task from lists
    runScript(scriptId, args) {
        let self = this;
        let prom = $.ajax('/api/task/' + scriptId, {
            dataType: 'text', // TODO JSON
            data: {arg: args},
            method: 'PUT'
        });

        prom.then(
            function (data) {
                return $.ajax('/api/task/' + data, {
                    dataType: 'json',
                    method: 'POST'
                });
            },
            function (err) {
                self.mainStore.addMessage('error', err);
            }
        ).then(
            function (data) {
                if (data.status === 'OK') {
                    self.mainStore.setActiveTab('tasks');
                    self.notifyChangeListeners('SCRIPT_ACTION_RUN');

                } else {
                    self.mainStore.addMessage('error', data.errors[0]);
                }
            },
            function (err) {
                self.mainStore.addMessage('error', err);
            }
        );
    }

    registerTask(scriptId, args) {
        let self = this;
        let prom = $.ajax('/api/task/' + scriptId, {
            dataType: 'text', // TODO JSON
            data: {arg: args},
            method: 'PUT'
        });

        prom.then(
            function (data) {
                // TODO maybe call tasksStore here and put the result there?
                self.mainStore.setActiveTab('tasks');
                self.notifyChangeListeners('SCRIPT_ACTION_REGISTER');
            },
            function (err) {
                self.notifyChangeListeners('ERROR', err);
                self.mainStore.addMessage('error', err);
            }
        );
    }

    getList() {
        return this.data;
    }

}