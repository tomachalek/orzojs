/*
 * Copyright (c) 2013 Tomas Machalek
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
package net.orzo.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * Classpath related resources loading. Please note that this class expects
 * package names using the slash character "/" i.e. org/myproject/foo
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class ResourceLoader {
	/**
	 * Returns input stream of provided resource name
	 * 
	 * @param ident
	 *            name of the resource
	 */
	public InputStream getResourceStream(String ident) {
		return getClass().getClassLoader().getResourceAsStream(ident);
	}

	/**
	 * Returns resource location as URL.
	 * 
	 * @param ident
	 *            resource name
	 */
	public URL getResourceUrl(String ident) {
		return getClass().getClassLoader().getResource(ident);
	}

	/**
	 * Loads resource and transforms it to a Properties object
	 * 
	 * @param ident
	 *            resource name
	 */
	public Properties getResourceProperties(String ident) throws IOException {
		Properties p = new Properties();
		p.load(getResourceStream(ident));
		return p;
	}

	/**
	 * Loads resource as a String. Method does not care whether the resource is
	 * a text or some binary data.
	 * 
	 * @param ident
	 *            resource name
	 * @return string content of the resource or null if resource does not exist
	 */
	public String getResourceAsString(String ident) throws IOException {
		InputStream is = getResourceStream(ident);
		if (is == null) {
			return null;
		}
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader bReader = new BufferedReader(reader);
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = bReader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		return buffer.toString();
	}

	/**
	 * Returns real filesystem path of package
	 * 
	 * @param pkg
	 *            package name
	 */
	public String getPackageFsPath(String pkg) {
		if (getClass().getClassLoader().getResource(pkg) != null) {
			return getClass().getClassLoader().getResource(pkg).getPath();
		}
		return null;
	}
}
