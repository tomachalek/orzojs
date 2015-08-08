/*
 * Copyright (C) 2014 Tomas Machalek
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

package net.orzo.lib;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public interface StringDistances {

	Integer levenshtein(String s1, String s2);

	Integer fuzzy(String s1, String s2, String locale);

	Double jaroWinkler(String s1, String s2);

	Double normalizedCompression(String s1, String s2);
}