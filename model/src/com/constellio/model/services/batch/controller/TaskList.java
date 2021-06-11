package com.constellio.model.services.batch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

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

	void addSubTask(Runnable runnable) {
		RecursiveTask recursiveTask = new RecursiveTask() {
			@Override
			protected Object compute() {
				List<String> errors = new ArrayList<>();
				try {
					runnable.run();
					onTaskFinished();
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error("Could not execute action", e);
				}
				return errors;
			}
		};
		totalTasksCount.incrementAndGet();
		recursiveTask.fork();
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
