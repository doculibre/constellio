package com.constellio.data.dao.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

public class Stats {

	private static Map<String, CallStatCompiler> stats = new HashMap<>();
	private static ThreadLocal<CallStatCompiler> currentStatCompiler = new ThreadLocal<>();
	private static ThreadLocal<Stack<CallStatCompiler>> previousStatCompilers = new ThreadLocal<>();

	private static CallStatCompiler NULL_COMPILER = new CallStatCompiler("null") {
		@Override
		public void decreaseCallFromChild(long callTime) {
		}

		@Override
		public void logCall(long callTime) {
		}

		@Override
		public void log(Runnable runnable) {
			runnable.run();
		}

		@Override
		public long start() {
			return 0;
		}

		@Override
		public void stop(long start) {
		}

		@Override
		public <T> T log(Supplier<T> supplier) {
			return supplier.get();
		}
	};

	public static CallStatCompiler compilerFor(Class<?> clazz) {
		return compilerFor(clazz.getSimpleName());
	}

	public static CallStatCompiler compilerFor(String statName) {

		if (statName == null) {
			return NULL_COMPILER;
		}

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

		public void decreaseCallFromChild(long callTime) {
			this.calls--;
			this.time -= callTime;
		}

		public void logCall(long callTime) {
			this.calls++;
			this.time += callTime;
		}

		public void log(Runnable runnable) {
			long start = start();
			try {
				runnable.run();
			} finally {
				stop(start);
			}

		}

		public long start() {
			long start = new Date().getTime();
			CallStatCompiler currentStatCompiler = Stats.currentStatCompiler.get();
			if (currentStatCompiler != null) {
				Stack<CallStatCompiler> previousStatCompilers = Stats.previousStatCompilers.get();
				if (previousStatCompilers == null) {
					previousStatCompilers = new Stack<>();
					Stats.previousStatCompilers.set(previousStatCompilers);
				}
				previousStatCompilers.add(currentStatCompiler);
			}
			Stats.currentStatCompiler.set(this);
			return start;

		}

		public void stop(long start) {
			long end = new Date().getTime();
			Stack<CallStatCompiler> previousStatCompilers = Stats.previousStatCompilers.get();
			if (previousStatCompilers != null) {
				previousStatCompilers.forEach((c) -> c.decreaseCallFromChild(end - start));
				Stats.currentStatCompiler.set(previousStatCompilers.pop());
				if (previousStatCompilers.isEmpty()) {
					Stats.previousStatCompilers.set(null);
				}
			} else {
				Stats.currentStatCompiler.set(null);
			}
			logCall(end - start);
		}

		public <T> T log(Supplier<T> supplier) {
			long start = start();
			try {
				return supplier.get();
			} finally {
				stop(start);
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
