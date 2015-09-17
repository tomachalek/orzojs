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
import {PopupBox} from './common';

export function scriptsTableFactory(dispatcher, scriptsStore) {

    var RegisterForm = React.createClass({

        _handleButtonClick : function () {
            let actionMap = {
                run: 'SCRIPT_ACTION_RUN',
                register: 'SCRIPT_ACTION_REGISTER'
            }
            dispatcher.dispatch({
                type: actionMap[this.props.actionType],
                value: {
                    itemId: this.props.itemId,
                    args: this.state.args
                }
            });
        },

        _handleInputChange : function (id, event) {
            let args = this.state.args;
            args[id] = event.target.value;
            this.setState({args: args});
        },

        getInitialState : function () {
            return {args: []};
        },

        render : function () {
            let inputs = [];
            this.props.args.forEach((item, inputId) => {
                inputs.push(
                    <li key={inputId}>
                        <label>arg. {inputId}:&nbsp;
                            <input type="text" style={{width: '15em'}}
                                   onChange={this._handleInputChange.bind(this, inputId)}
                                   defaultValue={item} />
                        </label>
                    </li>
                );
            });
            return (
                <form className="task-reg">
                    <ul className="input-args">
                    {inputs}
                    </ul>
                    <div className="submit">
                    <button type="button" onClick={this._handleButtonClick}>Register</button>
                    </div>
                </form>
            );
        }

    });


    return React.createClass({

        _handleScriptsStoreEvent : function (store, type, err) {
            if (type === 'SCRIPTS_LOAD') {
                this.setState({data: scriptsStore.getList(), error: null, scriptInForm: null});
                dispatcher.dispatch({
                    type: 'READY'
                });

            } else if (type === 'SCRIPT_ACTION_REGISTER'
                    || type === 'SCRIPT_ACTION_RUN') {
                this.setState({data: scriptsStore.getList(), error: null, scriptInForm: null});

            } else if (type === 'ERROR') {
                this.setState({data: [], error: err, scriptInForm: null});
                dispatcher.dispatch({
                    type: 'ERROR',
                    value: err
                });
            }
        },

        _handleActionClick : function (actionType, itemId) {
            this.setState({
                data: this.state.data,
                eror: this.state.error,
                scriptInForm: {actionType: actionType, itemId: itemId}
            });
        },

        getInitialState : function () {
            return {data : [], error: null, scriptInForm: null};
        },

        componentDidMount : function () {
            scriptsStore.addChangeListener(this._handleScriptsStoreEvent);
        },

        componentWillUnmount : function () {
            scriptsStore.removeChangeListener(this._handleScriptsStoreEvent);
        },

        _closeForm : function () {
            this.setState({
                data: this.state.data,
                eror: this.state.error,
                scriptInForm: null
            });
        },

    	render : function () {
            let self = this;
            let getArgs = (itemId) => {
                let defaultArgs = scriptsStore.getDefaultArgs(itemId);
                return defaultArgs.length > 0 ? defaultArgs : [null, null];
            };
            let rows = this.state.data.map(
                  item =>
                  <tr key={item.name}>
                    <td><a className="script-name">{item.name}</a></td>
                    <td style={{maxWidth: '25%'}}>{item.description}</td>
                    <td><code>[{item.defaultArgs.join(', ')}]</code></td>
                    <td><a className="action" onClick={self._handleActionClick.bind(self, 'run', item.id)}>new task &amp; run</a></td>
                    <td><a className="action" onClick={self._handleActionClick.bind(self, 'register', item.id)}>new task</a></td>
                  </tr>
                  );
    	   return (
                <div>
                    { this.state.scriptInForm ?
                        <PopupBox closeAction={this._closeForm}>
                            <RegisterForm itemId={this.state.scriptInForm.itemId}
                                actionType={this.state.scriptInForm.actionType}
                                args={getArgs(this.state.scriptInForm.itemId)} />
                        </PopupBox>
                        : null
                    }
                    <table>
                        <tbody>
                            <tr>
                                <th>name</th>
                                <th>description</th>
                                <th>default args</th>
                                <th colSpan="2">actions</th>
                            </tr>
                            {rows}
                        </tbody>
                    </table>
                </div>
            );
        }
    });
}