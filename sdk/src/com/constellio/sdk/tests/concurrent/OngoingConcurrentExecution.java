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
