package com.constellio.data.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CallCounter {

	private static Logger LOGGER = LoggerFactory.getLogger(CallCounter.class);

	private static Map<String, Map<Object, AtomicInteger>> counters = new HashMap<>();

	public static void addCall(String counterName, Object counterArg, int printEveryXCall) {

		Map<Object, AtomicInteger> argCounters = counters.get(counterName);
		if (argCounters == null) {
			synchronized (AtomicInteger.class) {
				argCounters = counters.get(counterName);
				if (argCounters == null) {
					argCounters = new HashMap<>();
					argCounters.put(counterArg, new AtomicInteger());
					counters.put(counterName, argCounters);
				}
			}
		}

		AtomicInteger counter = argCounters.get(counterArg);
		if (counter == null) {
			synchronized (AtomicInteger.class) {
				counter = argCounters.get(counterArg);
				if (counter == null) {
					counter = new AtomicInteger();
					argCounters.put(counterArg, counter);
				}
			}
		}

		int value = counter.incrementAndGet();
		if (value % printEveryXCall == 0) {
			LOGGER.info(counterName + "-" + counterArg + " : " + value + " calls");
		}
	}
}
