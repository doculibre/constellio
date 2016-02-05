package com.constellio.sdk.tests.concurrent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OngoingConcurrentExecution {

	private int numberOfThreads;

	private ConcurrentJob job;

	public OngoingConcurrentExecution(ConcurrentJob job) {
		this.job = job;
	}

	public OngoingConcurrentExecution withThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
		return this;
	}

	public ConcurrentExecutionResults untilAllWorkerHasFinished() {
		final AtomicInteger nbInvokations = new AtomicInteger();
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < numberOfThreads; i++) {
			final int worker = i;
			threads.add(new Thread() {

				@Override
				public void run() {
					nbInvokations.incrementAndGet();
					Map<String, Object> context = job.setupWorkerContext(worker);
					try {
						job.run(context, worker);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			});
		}
		return runUntilFinished(threads, nbInvokations);
	}

	public ConcurrentExecutionResults untilTotalInvokationOf(final int totalInvokation) {
		final AtomicInteger nbInvokations = new AtomicInteger();
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < numberOfThreads; i++) {
			final int worker = i;
			threads.add(new Thread() {

				@Override
				public void run() {
					Map<String, Object> context = job.setupWorkerContext(worker);
					boolean alive = true;
					while (alive) {
						synchronized (OngoingConcurrentExecution.class) {
							if (nbInvokations.get() >= totalInvokation) {
								alive = false;
							} else {
								nbInvokations.incrementAndGet();
							}
						}
						if (alive) {
							try {
								job.run(context, worker);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
				}

			});
		}
		return runUntilFinished(threads, nbInvokations);
	}

	private ConcurrentExecutionResults runUntilFinished(List<Thread> threads, AtomicInteger nbInvokations) {

		long start = new Date().getTime();
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		long end = new Date().getTime();
		return new ConcurrentExecutionResults(nbInvokations.get(), end - start);
	}
}
