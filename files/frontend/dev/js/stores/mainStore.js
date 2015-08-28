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

export class MainStore extends GeneralStore {

    constructor(dispatcher, logStore) {
        super(dispatcher);
        this.logStore = logStore;
        this.activeTab = null;
        this.dispatcherToken = this.dispatcher.register(this.handleEvent.bind(this));
        this.messages = [];
    }

    getLastMessage() {
        return this.messages.length > 0 ? this.messages[this.messages.length - 1] : null;
    }

    handleEvent(payload) {
        switch (payload.type) {
            case 'MAIN_MENU':
                this.activeTab = payload.value;
                this.notifyChangeListeners('MAIN_MENU');
                break;
            case 'READY':
                this.notifyChangeListeners(payload.type);
                this.messages.push(payload.value);
                break;
        }
    }

    setActiveTab(tabName) {
        this.activeTab = tabName;
        this.notifyChangeListeners('FOCUS_CHANGE');
    }

    getActiveTab() {
        return this.activeTab;
    }
}
