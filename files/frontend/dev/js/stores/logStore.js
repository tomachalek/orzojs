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

export class LogStore extends GeneralStore {

    constructor(dispatcher, mainStore) {
        super(dispatcher);
        this.data = [];
        this.mainStore = mainStore;
        this.dispatcherToken = this.dispatcher.register(this.handleEvent.bind(this));
    }

    handleEvent(payload) {
        switch (payload.type) {
            case 'LOG_LOAD':
                this.loadList();
                break;
        }
    }

    loadList() {
        let prom = $.ajax('/api/log', {
            dataType: 'json',
            method: 'GET'
        });
        let self = this;

        prom.then(
            function (data) {
                self.data = data;
                self.notifyChangeListeners('LOG_LOAD');
            },
            function (err) {
                self.mainStore.addMessage('error', err);
                self.notifyChangeListeners('ERROR', err);
            }
        );
    }

    getList() {
        return this.data;
    }

}
