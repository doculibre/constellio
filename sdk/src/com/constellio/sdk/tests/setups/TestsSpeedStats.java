package com.constellio.sdk.tests.setups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.sdk.tests.AbstractConstellioTest;

public class TestsSpeedStats {

	Map<String, TestSpeedStat> stats = new HashMap<>();

	public void add(AbstractConstellioTest test, String testName, String task, long duration) {
		TestSpeedStat stat = stats.get(task);
		if (stat == null) {
			stat = new TestSpeedStat(task);
			stats.put(task, stat);
		}
		stat.log(test, testName, duration);
	}

	public void printSummaries() {
		for (TestSpeedStat stat : stats.values()) {
			System.out.println(
					stat.task + " was called " + stat.count + " times (total of " + stat.total + "ms, average of " + (stat.total
							/ stat.count) + "ms)");
		}
	}

	private static class TestSpeedStat {

		private long total;

		private int count;

		private String task;

		private List<TestSpeedStatEntry> entries = new ArrayList<>();

		private TestSpeedStat(String task) {
			this.task = task;
		}

		public void log(AbstractConstellioTest test, String testName, long duration) {
			entries.add(new TestSpeedStatEntry(duration, test.getClass().getSimpleName() + "#" + testName));
			count++;
			total += duration;

		}
	}

	private static class TestSpeedStatEntry {

		private long duration;

		String test;

		private TestSpeedStatEntry(long duration, String test) {
			this.duration = duration;
			this.test = test;
		}
	}
}
