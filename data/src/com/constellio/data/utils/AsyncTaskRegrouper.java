package com.constellio.data.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.Duration;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;


public class AsyncTaskRegrouper<T> implements Closeable {

	@Getter
	@Setter
	private int sleepTime = 100;

	@Getter
	@Setter
	private int queueCapacity = 5000;

	private LinkedBlockingQueue<AsyncTaskRegrouperItem> queue = new LinkedBlockingQueue<>(queueCapacity);

	private LinkedBlockingQueue<List<AsyncTaskRegrouperItem>> regroupedQueue = new LinkedBlockingQueue<>(2);

	private Consumer<List<T>> consumer;

	@Getter
	@Setter
	private int maxExecutorThreads = 2;

	@Getter
	@Setter
	private Duration maxDelay;

	private Thread regrouperThread;

	private ThreadList executorThreads;

	private boolean active;

	public AsyncTaskRegrouper(Duration maxDelay, Consumer<List<T>> consumer) {
		this.consumer = consumer;
		this.maxDelay = maxDelay;
	}

	public void start() {
		active = true;
		executorThreads = ThreadList.running(maxExecutorThreads, () -> {
			while (active) {
				consumeRegrouped();
			}
		});

		executorThreads.startAll();

		regrouperThread = new Thread(() -> {
			while (active) {
				if (shouldRegroup()) {
					regroup();
				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		regrouperThread.start();
	}

	public void close() {
		active = false;
		for (int i = 0; i < maxExecutorThreads; i++) {
			try {
				regroupedQueue.put(Collections.emptyList());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			regrouperThread.join();
			executorThreads.joinAll();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void consumeRegrouped() {
		try {
			List<AsyncTaskRegrouperItem> job = regroupedQueue.take();

			if (!job.isEmpty()) {
				List<T> items = (List<T>) job.stream().map(AsyncTaskRegrouperItem::getItem).collect(toList());
				consumer.accept(items);
				for (AsyncTaskRegrouperItem item : job) {
					if (item.callback != null) {
						item.callback.run();
					}
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * We only regroup when we have to or when the queue is 80% full
	 *
	 * @return
	 */
	private boolean shouldRegroup() {
		long time = TimeProvider.getLocalDateTime().toDate().getTime();
		long sendAllBefore = time - (maxDelay.getMillis() - 2 * sleepTime);
		int queueSize = queue.size();
		return queueSize >= queueCapacity * 0.5 || (queueSize > 0 && queue.peek().timestamp <= sendAllBefore);
	}

	private void regroup() {

		List<AsyncTaskRegrouperItem> regroupedItems = new ArrayList<>();

		int queueSize = queue.size();
		for (int i = 0; i < queueSize; i++) {
			try {
				regroupedItems.add(queue.take());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			regroupedQueue.put(regroupedItems);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void addAsync(T item, Runnable callback) {
		long timestamp = TimeProvider.getLocalDateTime().toDate().getTime();
		try {
			queue.put(new AsyncTaskRegrouperItem<T>(timestamp, item, callback));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@AllArgsConstructor
	private static class AsyncTaskRegrouperItem<T> {

		long timestamp;

		@Getter
		T item;

		Runnable callback;
	}
}
