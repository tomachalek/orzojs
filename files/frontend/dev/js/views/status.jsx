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


var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;

export function statusBarFactory(dispatcher, mainStore) {
    return React.createClass({

        _handleMainStoreChange : function (store, action) {
            if (action === 'READY') {
                this.setState({status: 'ready', message: mainStore.getLastMessage()});

            } else {
                this.setState({status: 'working', message: null});
            }
        },

        getInitialState : function () {
            return {status: 'ready'};
        },

        componentDidMount: function () {
            mainStore.addChangeListener(this._handleMainStoreChange);
        },

        componentWillUnmount: function () {
            mainStore.removeChangeListener(this._handleMainStoreChange);
        },

        render : function () {
            var status;

            if (this.state.status === 'working') {
                status = (
                    <ReactCSSTransitionGroup transitionName="msganim">
                        <span>{this.state.status}
                        {this.state.message ? '(' + this.state.message + ')' : ''}
                        </span>
                    </ReactCSSTransitionGroup>
                );

            } else {
                status = <span>{this.state.status}
                {this.state.message ? '(' + this.state.message + ')' : ''}</span>;
            }

            return <div>{status}</div>;
        }
    });

}