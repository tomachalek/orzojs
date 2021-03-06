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

package net.orzo;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Miscellaneous helper functions.
 *
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public class Util {

    public static String normalizePath(String path) {
        if (File.separator.equals("/")) {
            return path;

        } else {
            return path.replace(File.separator, "/");
        }
    }

    public static String milliSecondsToHMS(long numMillis) {
        if (numMillis < 0) {
            return "-";
        }
        DecimalFormat restFormatter = new DecimalFormat("00.00");
        int numSeconds = (int)(numMillis / 1000);
        long rest = numMillis - numSeconds * 1000;
        int hours = numSeconds / 3600;
        int minutes = (numSeconds % 3600) / 60;
        double seconds = (numSeconds % 60) + rest / 1000.0;
        return String.format("%02d:%02d:%s", hours, minutes, restFormatter.format(seconds));
    }

}
