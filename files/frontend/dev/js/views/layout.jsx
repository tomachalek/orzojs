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

import * as React from 'react/addons';
import * as logViews from './log';
import * as scriptsViews from './scripts';
import * as tasksViews from './tasks';

function defaultTabFactory(dispatcher, mainStore) {
    return React.createClass({
        render: function () {
            return <p>Welcome to Orzo.js control panel!</p>;
        }
    });
}

export function mainMenuFactory(dispatcher, mainStore) {
    return React.createClass({

        _tabEvents : {
            'scripts': 'SCRIPTS_LOAD',
            'tasks': 'TASKS_LOAD',
            'log': 'LOG_LOAD'
        },

        _handleMenuClick : function (actionId) {
            dispatcher.dispatch({
                'type': 'MAIN_MENU',
                'value': actionId
            });
            if (this._tabEvents.hasOwnProperty(actionId)) {
                dispatcher.dispatch({
                    type: this._tabEvents[actionId]
                });

            } else {
                // TODO dispatch error
            }
        },

        _changeListener : function (store, action, err) {
            if (action === 'FOCUS_CHANGE') {
                dispatcher.dispatch({
                    type: this._tabEvents[mainStore.getActiveTab()]
                });
                this.setState({activeTab: mainStore.getActiveTab()});

            } else if (action === 'MAIN_MENU') {
                this.setState({activeTab: mainStore.getActiveTab()});
            }
        },

        componentDidMount : function () {
            mainStore.addChangeListener(this._changeListener);
        },

        componentWillUnmount : function () {
            mainStore.removeChangeListener(this._changeListener);
        },

        getInitialState : function () {
            return {activeTab: null};
        },

    	render : function () {
            let currFlag = (name) => name === this.state.activeTab ? 'active' : null;
            return (
                <ul>
                    <li className={currFlag('scripts')}><a onClick={this._handleMenuClick.bind(this, "scripts")}>scripts</a></li>
                    <li className={currFlag('tasks')}><a onClick={this._handleMenuClick.bind(this, "tasks")}>tasks</a></li>
                    <li className={currFlag('log')}><a onClick={this._handleMenuClick.bind(this, "log")}>log</a></li>
                </ul>
            );
    	}
    });
}


export function contentTabFactory(dispatcher, mainStore, logStore, scriptsStore, tasksStore) {
    return React.createClass({

        _handleMainStoreChange(store, eventType, err) {
            if (eventType === 'MAIN_MENU' || eventType === 'FOCUS_CHANGE') {
                this.setState({activeTab: this._getActiveTab()});

            } else if (eventType === 'ERROR') {
                console.log('error: ', err); // TODO
            }
        },

        _getActiveTab : function () {
            switch (mainStore.getActiveTab()) {
                case 'log':
                    return logViews.logTableFactory(dispatcher, logStore);
                case 'scripts':
                    return scriptsViews.scriptsTableFactory(dispatcher, scriptsStore);
                case 'tasks':
                    return tasksViews.tasksTableFactory(dispatcher, tasksStore);
                default:
                    return this.getInitialState().activeTab;

            }
        },

        getInitialState() {
            return {
                activeTab: defaultTabFactory(dispatcher, mainStore)
            };
        },

        componentDidMount: function () {
            mainStore.addChangeListener(this._handleMainStoreChange);
        },

        componentWillUnmount: function () {
            mainStore.removeChangeListener(this._handleMainStoreChange);
        },

        render : function () {
            return (
                <div className="tab-content">
                    {React.createElement(this.state.activeTab)}
                </div>
            );
        }
    });
}

