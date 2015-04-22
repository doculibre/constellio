/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.batch.controller;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskList {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskList.class);

	private AtomicInteger totalTasksCount = new AtomicInteger();
	private AtomicInteger finishedTasksCount = new AtomicInteger();

	private ForkJoinPool pool;

	public TaskList(ForkJoinPool pool) {
		this.pool = pool;
	}

	void addRootTask(BatchProcessTask task) {
		totalTasksCount.incrementAndGet();
		pool.execute(task);
	}

	void addSubTask(BatchProcessTask task) {
		totalTasksCount.incrementAndGet();
		task.fork();
	}

	void onTaskFinished() {
		finishedTasksCount.incrementAndGet();
		logProgression();
	}

	private void logProgression() {
		String progression = "Tasks progression : " + finishedTasksCount.get() + "/" + totalTasksCount.get();
		System.out.println(progression);
	}
}
