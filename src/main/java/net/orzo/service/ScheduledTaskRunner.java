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

package net.orzo.service;

import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
public class ScheduledTaskRunner {

	private final int startHour;

	private final int startMinute;

	private final int interval;

	private ScheduledFuture<?> future;

	private final ScheduledExecutorService executorService;

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTaskRunner.class);


	public ScheduledTaskRunner(ScheduledExecutorService executorService, int startHour, int startMinute, int interval) {
		this.executorService = executorService;
		this.startHour = startHour;
		this.startMinute = startMinute;
		this.interval = interval;
	}

	public int getStartHour() {
		return startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public int getInterval() {
		return interval;
	}

	/**
	 * 
	 * @param hours
	 * @param minutes
	 * @return
	 */
	private long calculateInitialDelay(int hours, int minutes) {
		Calendar startDate = Calendar.getInstance();
		Calendar currDate = Calendar.getInstance();
		if (3600 * hours + 60 * minutes <= 3600 * startDate.get(Calendar.HOUR_OF_DAY)
				+ 60 * startDate.get(Calendar.MINUTE)) {
			startDate.add(Calendar.DAY_OF_MONTH, 1);
		}
		startDate.set(Calendar.HOUR_OF_DAY, hours);
		startDate.set(Calendar.MINUTE, minutes);
		startDate.set(Calendar.SECOND, 0);
		return startDate.getTimeInMillis() - currDate.getTimeInMillis();
	}

	public void start(Task t) {
		final Runnable runner = new Runnable() {
			public void run() {
				LOG.info("Running scheduled task " + t.getName());
				t.run();
			}
		};
		this.future = executorService.scheduleAtFixedRate(runner,
				calculateInitialDelay(startHour, startMinute), interval * 1000, TimeUnit.MILLISECONDS);

	}

	public void cancel() {
		this.future.cancel(false);
	}
}
