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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 * 
 */
public class Strings {
	
	private static Logger LOG = LoggerFactory.getLogger(Strings.class);

	/**
	 * Prints an object to the standard output and appends a new line.
	 */
	public void print(Object o) {
		System.out.println(o);
	}

	/**
	 * 
	 * @param s
	 * @param args
	 */
	public void printf(String s, Object... args) {
		System.out.print(sprintf(s, args));
	}

	/**
	 * Works in a similar way to Java's String.format but with respect to JavaScript numeric type
	 * (= you can pass a JS number to the %d flag).
	 * 
	 * @param s
	 * @param args
	 * @return formatted string with flags replaced by passed values
	 */
	public Object sprintf(String s, Object... args) {
		// we must deal here with the problem that in JavaScript there is only a single numeric type
		// Number which is converted to Double. If a user wants to print integers, then %d flag does
		// not work.
		Pattern p = Pattern.compile("(?<!%)%(0[0-9]+|\\+|,|-|\\.\\d|\\d\\.\\d)?(s|d|f|n|tB|td|te|ty|tY|tl|tM|tp|tD)");		
		Matcher m = p.matcher(s);
		Object[] modArgs = new Object[args.length];
		int i = 0;
		while (m.find()) {
			if (m.group().endsWith("d") && args[i] instanceof Double) {				
				modArgs[i] = (int)Math.round((double)args[i]);
				if ((int)modArgs[i] != (double)args[i]) {
					LOG.warn(String.format("Lost precision in sprintf (%s rounded to %d)", (double)args[i], modArgs[i]));
				}
				
			} else {
				modArgs[i] = args[i];
			}
			i++;
		}
		return String.format(s, modArgs); // TODO wrapping ???
	}

	/**
	 * 
	 */
	public Object md5(String value) {
		return DigestUtils.md5Hex(value);
	}
	
	public static void main(String[] args) {
		String s = "%%d foo %1.3f, ## %04d ## %s";
		String s2 = (String) new Strings().sprintf(s, 3.1416, 1.7, "A");
		System.out.println(s2);
	}

}
