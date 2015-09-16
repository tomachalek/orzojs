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


var views = require('./views/layout');
import * as React from 'react/addons';
var $ = require('jquery');
var Dispatcher = require('./vendor/Dispatcher').Dispatcher;

import {MainStore} from "./stores/mainStore";
import {LogStore} from "./stores/logStore";
import {ScriptsStore} from "./stores/scriptsStore";
import {TasksStore} from "./stores/tasksStore";
import  * as StatusBarView from "./views/status";

class OrzoSPA {

    constructor() {
        this.dispatcher = new Dispatcher();
        this.mainStore = new MainStore(this.dispatcher);
        this.logStore = new LogStore(this.dispatcher, this.mainStore);
        this.tasksStore = new TasksStore(this.dispatcher, this.mainStore);
        this.scriptsStore = new ScriptsStore(this.dispatcher, this.tasksStore, this.mainStore);
    }

    init() {
        React.render(React.createElement(views.mainMenuFactory(this.dispatcher,
                                         this.mainStore)),
                     document.getElementById('menu'));
        React.render(React.createElement(StatusBarView.statusBarFactory(this.dispatcher,
            this.mainStore)), document.getElementById('status-bar'));
        React.render(React.createElement(views.contentTabFactory(this.dispatcher,
                                                                 this.mainStore,
                                                                 this.logStore,
                                                                 this.scriptsStore,
                                                                 this.tasksStore)),
                     document.getElementById('content'));

    }
};

window.OrzoSPA = new OrzoSPA();
