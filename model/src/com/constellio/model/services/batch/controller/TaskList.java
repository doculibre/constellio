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
		int totalTasksCountValue = totalTasksCount.get();
		if (totalTasksCountValue > 5) {
			String progression = "Tasks progression : " + finishedTasksCount.get() + "/" + totalTasksCountValue;
			LOGGER.info(progression);
		}
	}
}
