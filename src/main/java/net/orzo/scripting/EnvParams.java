/*
 * Copyright (C) 2013 Tomas Machalek
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
package net.orzo.scripting;

import java.util.List;

/**
 * This class wraps some essential parameters all Orzo's JavaScript processing
 * needs.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class EnvParams {

	/**
	 * Identifies calculation worker
	 */
	public int workerId;

	/**
	 *  
	 */
	public String scriptName;

	/**
	 * 
	 */
	public String workingDir;

	/**
	 * Contains parameters user provided via command line
	 */
	public String[] inputArgs;

	/**
	 * Paths where Orzo looks when CommonJS require() is used
	 */
	public List<String> modulesPaths;

}
