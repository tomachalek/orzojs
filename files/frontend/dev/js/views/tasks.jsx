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
import {PopupBox, ResultView} from './common';


export function tasksTableFactory(dispatcher, tasksStore) {

    var ScheduleForm = React.createClass({

        _handleTimeChange : function (evt) {
            this.setState({initHour: evt.target.value, interval: this.state.interval});
        },

        _handleIntervalChange : function (evt) {
            this.setState({initHour: this.state.initHour, interval: evt.target.value});
        },

        getInitialState : function () {
            return {initHour: null, interval: null};
        },

        _handleScheduledRunClick : function (taskId) {
            dispatcher.dispatch({
                type: 'TASK_SCHEDULE',
                value: {
                    taskId: this.props.taskId,
                    initHour: this.state.initHour,
                    interval: this.state.interval
                }
            });
        },

        render : function () {
            return (
                <form>
                    <div>
                        <label>start at:
                            <input type="text" onChange={this._handleTimeChange} /> (hh:mm)</label>
                    </div>
                    <div>
                        <label>repeat each: <input type="text" onChange={this._handleIntervalChange} /> seconds</label>
                    </div>
                    <div>
                        <button type="button"
                                onClick={this._handleScheduledRunClick}>Schedule</button>
                    </div>
                </form>
            );
        }
    });

    return React.createClass({

        _handleTasksStoreEvent : function (store, event, err) {
            if (event === 'TASKS_LOAD') {
                this.setState({
                    data: store.getTasks(),
                    scheduleForm: null,
                    currentResult: null
                });
                dispatcher.dispatch({
                    type: 'READY',
                    value: 'tasks loaded'
                });

            } else if (event === 'TASK_SCHEDULE') {
                this.setState({
                    data: this.state.data,
                    scheduleForm: null,
                    currentResult: null
                });
                dispatcher.dispatch({
                    type: 'READY',
                    value: 'task scheduled'
                });

            } else if (event === 'TASK_GET_RESULT') {
                this.setState({
                    data: this.state.data,
                    scheduleForm: null,
                    currentResult: tasksStore.getCurrentResult()
                });

            } else if (event === 'TASK_DELETE') {
                this.setState({
                    data: this.state.data,
                    scheduleForm: null,
                    currentResult: null
                });

            } else if (event === 'ERROR') {
                dispatcher.dispatch({
                    type: 'ERROR',
                    value: err
                });
            }

        },

        _handleRunActionClick : function (taskId, event) {
            dispatcher.dispatch({
                type: 'TASK_RUN',
                value: taskId
            });
        },

        _handleDeleteTaskClick : function (taskId, event) {
            dispatcher.dispatch({
                type: 'TASK_DELETE',
                value: taskId
            });
        },

        _handleScheduleActionClick : function (taskId, event) {
            //React.addons.update(this.state, {scheduleForm: {$set: taskId}});
            this.setState({
                data: this.state.data,
                scheduleForm: taskId,
                currentResult: null
            });
        },

        _resultClickHandler : function (taskId) {
            dispatcher.dispatch({
                type: 'TASK_GET_RESULT',
                value: taskId
            });
        },

        _resultLink : function (item) {
            if (item.status === 'ERROR' || item.status === 'FINISHED') {
                return <a className="action" onClick={this._resultClickHandler.bind(this, item.id)}>{item.status}</a>;

            } else {
                return <span>{item.status}</span>;
            }
        },

        _closeResult : function () {
            this.setState({
                data: this.state.data,
                scheduleForm: this.state.scheduleForm,
                currentResult: null
            });
        },
        
        _closeScheduleForm : function () {
            this.setState({
                data: this.state.data,
                scheduleForm: null,
                currentResult: this.state.currentResult
            });
        },

        getInitialState : function () {
            return {data: [], scheduleForm: null};
        },

        componentDidMount : function () {
            tasksStore.addChangeListener(this._handleTasksStoreEvent);
        },

        componentWillUnmount : function () {
            tasksStore.removeChangeListener(this._handleTasksStoreEvent);
        },

        render: function () {
            let itemAction = (item) => {
                if (item.isScheduled) {
                    return (
                        <span>
                            <a className="action" onClick={self._handleDeleteTaskClick.bind(self, item.id)}>delete</a>
                        </span>
                    );

                } else {
                    return (
                        <span>
                            <a className="action" onClick={self._handleRunActionClick.bind(self, item.id)}>run now</a>
                            {'\u00a0|\u00a0'}
                            <a className="action" onClick={self._handleScheduleActionClick.bind(self, item.id)}>schedule</a>
                            {'\u00a0|\u00a0'}
                            <a className="action" onClick={self._handleDeleteTaskClick.bind(self, item.id)}>delete</a>

                        </span>
                    );
                }
            };
            let self = this;
            let rows = this.state.data.map(function (item) {
                return (
                    <tr key={item.id}>
                        <td data-id={item.id}>{item.id.substr(0, 8)}</td>
                        <td>{item.name}</td>
                        <td>{new Date(item.created).toLocaleString()}</td>
                        <td>{self._resultLink(item)}</td>
                        <td>
                            {item.isScheduled
                                ? item.startHour + ':' + item.startMinute + ' each '
                                    + item.interval + ' secs.'
                                : 'not scheduled'}
                        </td>
                        <td>
                            {itemAction(item)}
                        </td>
                    </tr>
                );
            });
            return (
                <div>
                    {
                        this.state.scheduleForm ? 
                        <PopupBox closeAction={this._closeScheduleForm}>
                            <ScheduleForm taskId={this.state.scheduleForm} />
                        </PopupBox> 
                        : null
                    }
                    {this.state.currentResult ?
                        <PopupBox closeAction={this._closeResult}>
                            <ResultView data={this.state.currentResult}></ResultView>
                        </PopupBox>
                        : null
                    }
                    <table>
                        <tbody>
                            <tr>
                                <th>id</th>
                                <th>name</th>
                                <th>created</th>
                                <th>status</th>
                                <th>scheduling</th>
                                <th>actions</th>
                            </tr>
                            {rows}
                        </tbody>
                    </table>
                </div>
            );
        }
	});
}