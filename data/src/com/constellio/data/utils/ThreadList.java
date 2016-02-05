package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadList<T extends Thread> {

	private final List<T> threads = new ArrayList<T>();

	public void add(T thread) {
		threads.add(thread);
	}

	public void addAndStart(T thread) {
		threads.add(thread);
		thread.start();
	}

	public void startAll()
			throws InterruptedException {
		for (Thread thread : threads) {
			thread.start();
		}
	}

	public void joinAll()
			throws InterruptedException {
		for (Thread thread : threads) {
			while (thread.isAlive()) {
				thread.join();
			}
		}
	}

	public List<T> getThreads() {
		return Collections.unmodifiableList(threads);
	}

	public int size() {
		return threads.size();
	}
}
