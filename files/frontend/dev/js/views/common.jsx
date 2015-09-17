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


export var PopupBox = React.createClass({
    _handleCloseClick : function () {
        if (this.props.closeAction) {
            this.props.closeAction();
        }
    },
    render : function () {
        return (
            <div className="popup">
                <div className="toolbar">
                    <img className="close" src="/dist/img/High-contrast-dialog-close_24px.png"
                        alt="close" title="close" onClick={this._handleCloseClick} />
                </div>
                <div className="content">
                    {this.props.children}
                </div>
            </div>
        );
    }
});


export var ResultView = React.createClass({
    _renderError : function () {
        return (
            <div className="result">
                <h2>{this.props.data.message}</h2>
                <pre>
                { this.props.data.errors.join('\n') }
                </pre>
            </div>
        );
    },
    _renderDefault : function () {
        return (
            <div className="result">
                <pre>
                {JSON.stringify(this.props.data, null, '    ')}
                </pre>
            </div>
        );
    },
    render : function () {
        if (this.props.data.status === 'ERROR') {
            return this._renderError();

        } else {
            return this._renderDefault();
        }
    }
});