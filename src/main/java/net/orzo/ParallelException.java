/*
 * Copyright (C) 2014 Tomas Machalek <tomas.machalek@gmail.com>
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

package net.orzo;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class ParallelException extends CalculationException {

    /**
     *
     */
    private static final long serialVersionUID = -2709776634004687856L;

    List<Exception> errors;

    public ParallelException(String message, List<Exception> errors) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.addAll(errors);
    }

    public ParallelException(String message, Exception error) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }

    @Override
    public List<Exception> getAllErrors() {
        return this.errors;
    }

}
