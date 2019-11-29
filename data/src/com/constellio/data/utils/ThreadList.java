package com.constellio.data.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadList<T extends Thread> {

	private final List<T> threads = new ArrayList<T>();

	public ThreadList() {
	}

	public ThreadList<T> add(T thread) {
		threads.add(thread);
		return this;
	}

	public ThreadList<T> addAndStart(T thread) {
		threads.add(thread);
		thread.start();
		return this;
	}

	public ThreadList<T> startAll() {
		for (Thread thread : threads) {
			thread.start();
		}
		return this;
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

	public static ThreadList<Thread> running(int nbThreads, Runnable runnable) {
		ThreadList<Thread> threads = new ThreadList<>();

		for (int i = 0; i < nbThreads; i++) {
			threads.add(new Thread(runnable));
		}
		return threads;
	}
}
