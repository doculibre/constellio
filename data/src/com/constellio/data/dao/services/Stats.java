package com.constellio.data.dao.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Stats {

	private static Map<String, CallStatCompiler> stats = new HashMap<>();

	public static CallStatCompiler compilerFor(String statName) {

		CallStatCompiler value = stats.get(statName);
		if (value == null) {
			synchronized (Stats.class) {
				value = stats.get(statName);
				if (value == null) {
					value = new CallStatCompiler(statName);
					stats.put(statName, value);
				}
			}
		}

		return value;
	}

	private static ThreadLocal<CallStatCompiler> currentStatCompiler = new ThreadLocal<>();

	/**
	 * For the moment, no distinction between group and name
	 *
	 * @return
	 */
	public static String getCurrentGroup() {
		CallStatCompiler callStatCompiler = getCurrentStatCompiler();
		return callStatCompiler == null ? null : callStatCompiler.getName();
	}

	public static String getCurrentName() {
		CallStatCompiler callStatCompiler = getCurrentStatCompiler();
		return callStatCompiler == null ? null : callStatCompiler.getName();
	}

	public static CallStatCompiler getCurrentStatCompiler() {
		return currentStatCompiler.get();
	}

	public static synchronized Map<String, CallStatCompiler> getStats() {
		Map<String, CallStatCompiler> statsCopy = new HashMap<>();
		for (Map.Entry<String, CallStatCompiler> entry : stats.entrySet()) {
			statsCopy.put(entry.getKey(), entry.getValue());
		}
		return statsCopy;
	}

	public static void reset() {
		stats = new HashMap<>();
	}

	public static class CallStatCompiler {

		String name;
		long calls;
		long time;

		public CallStatCompiler(String name) {
			this.name = name;
		}

		public void logCall(long callTime) {
			this.calls++;
			this.time += callTime;
		}

		public void log(Runnable runnable) {
			long start = new Date().getTime();
			CallStatCompiler previousStatCompiler = currentStatCompiler.get();
			currentStatCompiler.set(this);
			try {
				runnable.run();
			} finally {
				currentStatCompiler.set(previousStatCompiler);
				long end = new Date().getTime();
				logCall(end - start);
			}

		}

		public <T> T log(Supplier<T> supplier) {
			long start = new Date().getTime();
			CallStatCompiler previousStatCompiler = currentStatCompiler.get();
			currentStatCompiler.set(this);
			try {
				return supplier.get();
			} finally {
				currentStatCompiler.set(previousStatCompiler);
				long end = new Date().getTime();
				logCall(end - start);
			}

		}

		public String getName() {
			return name;
		}

		public long getCalls() {
			return calls;
		}

		public long getTime() {
			return time;
		}
	}
}
