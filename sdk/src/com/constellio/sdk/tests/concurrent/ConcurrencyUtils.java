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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrencyUtils {

	public static void concurrentIntegerFor(int nbthreads, int start, int end, int increment, final IncrementForTask task,
			final WorkerContextFactory workerContextFactory) {

		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>((int) Math.ceil((end - start) / increment));
		for (int i = start; i < end; i += increment) {
			queue.add(i);
		}

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < nbthreads; i++) {
			final int workerId = i;
			threads.add(new Thread() {

				@Override
				public void run() {
					Map<String, Object> context = new HashMap<String, Object>();
					workerContextFactory.setupWorkerContext(workerId, context);
					Integer nextTask;
					while ((nextTask = queue.poll()) != null) {
						task.executeTask(nextTask, context);
					}
				}

			});
		}

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

	}

	public interface IncrementForTask {
		void executeTask(int i, Map<String, Object> workerContext);
	}

	public interface WorkerContextFactory {

		void setupWorkerContext(int worker, Map<String, Object> workerContext);
	}
}
