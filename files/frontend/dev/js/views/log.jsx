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

export function logTableFactory(dispatcher, logStore) {

    return React.createClass({

        _handleLogStoreEvent : function (store, type, err) {
            if (type === 'LOG_LOAD') {
                this.setState({data: logStore.getList(), detail: null});
                dispatcher.dispatch({
                    type: 'READY'
                });

            } else if (type === 'ERROR') {
                dispatcher.dispatch({
                    type: 'ERROR',
                    value: err
                });
            }
        },

        _handleStatusClick : function () {

        },

        getInitialState : function () {
            return {'data' : [], 'detail': null};
        },

        componentDidMount : function () {
            logStore.addChangeListener(this._handleLogStoreEvent);
        },

        componentWillUnmount : function () {
            logStore.removeChangeListener(this._handleLogStoreEvent);
        },

        _openDetail : function (item) {
            this.setState({data: this.state.data, detail: item.err});
        },

        _renderStatus : function (item) {
            if (item.status === 'ERROR') {
                return <a className="action"
                        onClick={this._openDetail.bind(this, item)}>{item.status}</a>;

            } else {
                return <span>{item.status}</span>;
            }
        },

        _closeDetail : function () {
            this.setState({data: this.state.data, detail: null});
        },

    	render : function () {
            let self = this;
            let rows = this.state.data.map(
                  item =>
                  <tr key={item.row}>
                    <td>{new Date(item.started).toLocaleString()}</td>
                    <td>{item.name}</td>
                    <td data-id={item.id}>{item.id.substr(0, 8)}</td>
                    <td>{self._renderStatus(item)}</td>
                  </tr>
                  );
    	   return (
                <div>
                    {this.state.detail ?
                        <PopupBox closeAction={this._closeDetail}><div>{this.state.detail}</div></PopupBox>
                        : null
                    }
                    <table>
                        <tbody>
                            <tr>
                                <th>date and time</th>
                                <th>script</th>
                                <th>task id</th>
                                <th>event</th>
                            </tr>
                            {rows}
                        </tbody>
                    </table>
                </div>
            );
        }
    });
}