package com.constellio.data.utils;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadUtils {

	LinkedBlockingQueue<TaskObject> queue;

	public static <V> void iterateOverRunningTaskInParallel(Iterator<V> iterator, int threadsCount,
			IteratorElementTask<V> task)
			throws Exception {

		if (threadsCount == 1) {

			while (iterator.hasNext()) {
				task.executeTask(iterator.next());
			}

		} else {
			LinkedBlockingQueue<TaskObject<V>> queue = new LinkedBlockingQueue<>(threadsCount);
			ThreadList<QueueConsumerThread<V>> threadList = new ThreadList<>();
			AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();

			try {
				for (int i = 0; i < threadsCount; i++) {
					threadList.addAndStart(new QueueConsumerThread<>(queue, task, exceptionAtomicReference));
				}
				while (iterator.hasNext()) {
					queue.put(new TaskObject(iterator.next()));
				}
				if (exceptionAtomicReference.get() != null) {
					throw exceptionAtomicReference.get();
				}
			} finally {

				for (int i = 0; i < threadsCount; i++) {
					queue.put(new TaskObject(null));
				}

				threadList.joinAll();
			}
		}

	}

	public interface IteratorElementTask<V> {

		void executeTask(V value)
				throws Exception;

	}

	private static class TaskObject<V> {

		private V value;

		public TaskObject(V value) {
			this.value = value;
		}
	}

	private static class QueueConsumerThread<V> extends Thread {

		IteratorElementTask<V> task;

		LinkedBlockingQueue<TaskObject<V>> queue;

		AtomicReference<Exception> exceptionAtomicReference;

		public QueueConsumerThread(LinkedBlockingQueue<TaskObject<V>> queue, IteratorElementTask<V> task,
				AtomicReference<Exception> exceptionAtomicReference) {
			this.queue = queue;
			this.task = task;
			this.exceptionAtomicReference = exceptionAtomicReference;
		}

		@Override
		public void run() {
			while (true) {
				try {
					TaskObject<V> element = queue.take();

					if (element.value == null) {
						return;
					} else {
						try {
							task.executeTask(element.value);
						} catch (Exception e) {
							if (exceptionAtomicReference.get() == null) {
								exceptionAtomicReference.set(e);
							} else {
								e.printStackTrace();
							}
						}
					}

				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
