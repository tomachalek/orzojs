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
            if (action === 'MESSAGE') {
                let [msgType, message] = mainStore.getLastMessage();
                this.setState({msgType: msgType, message: message});
            }
        },

        getInitialState : function () {
            return {msgType: 'info'};
        },

        componentDidMount: function () {
            mainStore.addChangeListener(this._handleMainStoreChange);
        },

        componentWillUnmount: function () {
            mainStore.removeChangeListener(this._handleMainStoreChange);
        },

        _handleDismissClick : function () {
            this.setState({msgType: null, message: null});
        },

        render : function () {
            var status;

            if (this.state.msgType === 'working') {
                return (
                    <div className="messages">
                        <ReactCSSTransitionGroup transitionName="msganim">
                            <span className={'message ' + this.state.msgType}>
                                {this.state.message ? '(' + this.state.message + ')' : ''}
                            </span>
                        </ReactCSSTransitionGroup>
                    </div>
                );

            } else if (this.state.message) {
                return (
                    <div className="messages">
                        <span className={'message ' + this.state.msgType}>
                            {this.state.message ? this.state.message : ''}
                        </span>
                        (<a className="dismiss" onClick={this._handleDismissClick}>dismiss</a>)
                    </div>
                );

            } else {
                return <div className="messages" />;
            }
        }
    });

}